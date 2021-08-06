/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.project;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.lang.jsgraphql.endpoint.psi.*;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLNamedScope;
import com.intellij.lang.jsgraphql.schema.GraphQLRegistryInfo;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeListener;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.v1.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.lang.jsgraphql.v1.schema.ide.type.JSGraphQLNamedType;
import com.intellij.lang.jsgraphql.v1.schema.ide.type.JSGraphQLNamedTypeRegistry;
import com.intellij.lang.jsgraphql.v1.schema.ide.type.JSGraphQLPropertyType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Registry for resolving references to PSI Elements in the Endpoint language.
 */
public class JSGraphQLEndpointNamedTypeRegistry implements JSGraphQLNamedTypeRegistry, Disposable {

    private final JSGraphQLConfigurationProvider configurationProvider;
    private final GraphQLConfigManager graphQLConfigManager;
    private final Project project;

    private final Map<GraphQLNamedScope, Map<String, JSGraphQLNamedType>> endpointTypesByName = Maps.newConcurrentMap();
    private final Map<GraphQLNamedScope, PsiFile> endpointEntryPsiFile = Maps.newConcurrentMap();
    private final Map<GraphQLNamedScope, GraphQLRegistryInfo> projectToRegistry = Maps.newConcurrentMap();

    public static JSGraphQLEndpointNamedTypeRegistry getService(@NotNull Project project) {
        return ServiceManager.getService(project, JSGraphQLEndpointNamedTypeRegistry.class);
    }

    public JSGraphQLEndpointNamedTypeRegistry(Project project) {
        this.project = project;
        this.configurationProvider = JSGraphQLConfigurationProvider.getService(project);
        graphQLConfigManager = GraphQLConfigManager.getService(project);
        project.getMessageBus().connect(this).subscribe(GraphQLSchemaChangeListener.TOPIC, schemaVersion -> {
            endpointTypesByName.clear();
            endpointEntryPsiFile.clear();
            projectToRegistry.clear();
        });
    }

    private PsiFile getEndpointEntryPsiFile(PsiElement scopedPsiElement) {
        final GraphQLNamedScope schemaScope = getSchemaScope(scopedPsiElement);
        if(schemaScope == null) {
            return null;
        }
        return endpointEntryPsiFile.computeIfAbsent(schemaScope, p -> {
            final VirtualFile endpointEntryFile = configurationProvider.getEndpointEntryFile(scopedPsiElement.getContainingFile().getOriginalFile());
            if (endpointEntryFile != null) {
                return PsiManager.getInstance(project).findFile(endpointEntryFile);
            }
            return null;
        });
    }

    @Override
    public JSGraphQLNamedType getNamedType(String typeNameToGet, PsiElement scopedElement) {
        return computeNamedTypes(scopedElement).get(typeNameToGet);
    }

    public void enumerateTypes(PsiElement scopedElement, Consumer<JSGraphQLNamedType> consumer) {
        computeNamedTypes(scopedElement).forEach((key, jsGraphQLNamedType) -> consumer.accept(jsGraphQLNamedType));
    }

    public GraphQLRegistryInfo getTypesAsRegistry(PsiElement scopedElement) {
        final GraphQLNamedScope schemaScope = getSchemaScope(scopedElement);
        if (schemaScope == null) {
            return new GraphQLRegistryInfo(new TypeDefinitionRegistry(), Collections.emptyList(), false);
        }
        return projectToRegistry.computeIfAbsent(schemaScope, p -> doGetTypesAsRegistry(scopedElement));
    }

    private GraphQLNamedScope getSchemaScope(PsiElement scopedElement) {
        PsiFile containingFile = scopedElement.getContainingFile();
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            // in memory PsiFile such as the completion PSI
            virtualFile = containingFile.getOriginalFile().getVirtualFile();
        }
        return virtualFile != null ? graphQLConfigManager.getSchemaScope(virtualFile) : null;
    }

    private GraphQLRegistryInfo doGetTypesAsRegistry(PsiElement scopedElement) {

        final TypeDefinitionRegistry registry = new TypeDefinitionRegistry();
        final List<GraphQLException> errors = Lists.newArrayList();

        final Map<String, JSGraphQLNamedType> namedTypes = computeNamedTypes(scopedElement);

        final PsiRecursiveElementVisitor errorsVisitor = new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if(element instanceof PsiErrorElement) {
                    errors.add(new JSGraphQLEndpointSchemaError("Syntax error in '" + element.getContainingFile().getName() + "': " + ((PsiErrorElement) element).getErrorDescription(), element));
                }
                super.visitElement(element);
            }
        };

        namedTypes.forEach((name, endpointType) -> {
            final PsiElement psiDefinition = endpointType.definitionElement;

            // add syntax errors as schema errors
            if(psiDefinition != null) {
                psiDefinition.accept(errorsVisitor);
            }

            if (psiDefinition instanceof JSGraphQLEndpointObjectTypeDefinition) {
                final JSGraphQLEndpointObjectTypeDefinition typeDefinition = (JSGraphQLEndpointObjectTypeDefinition) psiDefinition;
                final List<FieldDefinition> fieldDefinitions = Lists.newArrayList();
                final Set<String> addedFieldNames = Sets.newHashSet();
                if (typeDefinition.getFieldDefinitionSet() != null) {
                    final List<JSGraphQLEndpointFieldDefinition> fieldDefinitionList = typeDefinition.getFieldDefinitionSet().getFieldDefinitionList();
                    for (JSGraphQLEndpointFieldDefinition endpointFieldDefinition : fieldDefinitionList) {
                        addFieldDefinition(fieldDefinitions, addedFieldNames, endpointFieldDefinition, errors);
                    }
                }
                final List<Type> interfaces;
                final JSGraphQLEndpointImplementsInterfaces interfacesPsi = typeDefinition.getImplementsInterfaces();
                if (interfacesPsi != null) {
                    final List<JSGraphQLEndpointNamedType> namedTypeList = interfacesPsi.getNamedTypeList();
                    interfaces = Lists.newArrayListWithExpectedSize(namedTypeList.size());
                    for (JSGraphQLEndpointNamedType endpointImplementedType : namedTypeList) {
                        final JSGraphQLNamedType implementedType = namedTypes.get(endpointImplementedType.getName());
                        if (implementedType != null) {
                            interfaces.add(new TypeName(endpointImplementedType.getName()));
                            if (implementedType.definitionElement instanceof JSGraphQLEndpointInterfaceTypeDefinition) {
                                final JSGraphQLEndpointFieldDefinitionSet fieldDefinitionSet = ((JSGraphQLEndpointInterfaceTypeDefinition) implementedType.definitionElement).getFieldDefinitionSet();
                                if (fieldDefinitionSet != null) {
                                    for (JSGraphQLEndpointFieldDefinition interfaceFieldDefinition : fieldDefinitionSet.getFieldDefinitionList()) {
                                        addFieldDefinition(fieldDefinitions, addedFieldNames, interfaceFieldDefinition, errors);
                                    }
                                }
                            }
                        } else {
                            errors.add(new JSGraphQLEndpointSchemaError("Unable to resolve interface Type '" + endpointImplementedType.getName() + "'", psiDefinition));
                        }
                    }
                } else {
                    interfaces = Collections.emptyList();
                }

                final ObjectTypeDefinition.Builder builder = ObjectTypeDefinition.newObjectTypeDefinition();
                final SourceLocation sourceLocation = getSourceLocation(typeDefinition);
                final Description description = getDescription(typeDefinition, sourceLocation);
                builder.name(endpointType.getName()).implementz(interfaces).fieldDefinitions(fieldDefinitions).sourceLocation(sourceLocation).description(description);

                registry.add(builder.build());

            } else if (psiDefinition instanceof JSGraphQLEndpointInterfaceTypeDefinition) {

                final JSGraphQLEndpointInterfaceTypeDefinition psiInterfaceDefinition = (JSGraphQLEndpointInterfaceTypeDefinition) psiDefinition;
                if (psiInterfaceDefinition.getNamedTypeDef() != null) {
                    final List<FieldDefinition> fieldDefinitions = Lists.newArrayList();
                    if (psiInterfaceDefinition.getFieldDefinitionSet() != null) {
                        final List<JSGraphQLEndpointFieldDefinition> fieldDefinitionList = psiInterfaceDefinition.getFieldDefinitionSet().getFieldDefinitionList();
                        final Set<String> addedFieldNames = Sets.newHashSet();
                        for (JSGraphQLEndpointFieldDefinition endpointFieldDefinition : fieldDefinitionList) {
                            addFieldDefinition(fieldDefinitions, addedFieldNames, endpointFieldDefinition, errors);
                        }
                    }

                    final InterfaceTypeDefinition.Builder builder = InterfaceTypeDefinition.newInterfaceTypeDefinition();
                    final SourceLocation sourceLocation = getSourceLocation(psiDefinition);
                    final Description description = getDescription(psiInterfaceDefinition, sourceLocation);
                    builder.name(psiInterfaceDefinition.getNamedTypeDef().getName()).definitions(fieldDefinitions).sourceLocation(sourceLocation).description(description);
                    registry.add(builder.build());
                }

            } else if (psiDefinition instanceof JSGraphQLEndpointInputObjectTypeDefinition) {

                final JSGraphQLEndpointInputObjectTypeDefinition psiInputObjectDefinition = (JSGraphQLEndpointInputObjectTypeDefinition) psiDefinition;
                if (psiInputObjectDefinition.getNamedTypeDef() != null) {
                    final List<InputValueDefinition> inputValueDefinitions = Lists.newArrayList();
                    if (psiInputObjectDefinition.getFieldDefinitionSet() != null) {
                        for (JSGraphQLEndpointFieldDefinition fieldDefinition : psiInputObjectDefinition.getFieldDefinitionSet().getFieldDefinitionList()) {
                            if (fieldDefinition.getCompositeType() != null) {
                                final InputValueDefinition inputValueDefinition = InputValueDefinition.newInputValueDefinition()
                                        .name(fieldDefinition.getProperty().getName())
                                        .type(createType(fieldDefinition.getCompositeType()))
                                        .build();
                                inputValueDefinitions.add(inputValueDefinition);
                            }
                        }
                    }
                    final InputObjectTypeDefinition.Builder builder = InputObjectTypeDefinition.newInputObjectDefinition();
                    final SourceLocation sourceLocation = getSourceLocation(psiDefinition);
                    builder.name(psiInputObjectDefinition.getNamedTypeDef().getName()).inputValueDefinitions(inputValueDefinitions).sourceLocation(sourceLocation);
                    registry.add(builder.build());

                }
            } else if (psiDefinition instanceof JSGraphQLEndpointEnumTypeDefinition) {

                final JSGraphQLEndpointEnumTypeDefinition psiEnumTypeDefinition = (JSGraphQLEndpointEnumTypeDefinition) psiDefinition;
                if (psiEnumTypeDefinition.getNamedTypeDef() != null) {
                    final List<EnumValueDefinition> enumValueDefinitions = Lists.newArrayList();
                    if (psiEnumTypeDefinition.getEnumValueDefinitionSet() != null) {
                        for (JSGraphQLEndpointEnumValueDefinition psiEnumValueDefinition : psiEnumTypeDefinition.getEnumValueDefinitionSet().getEnumValueDefinitionList()) {
                            enumValueDefinitions.add(new EnumValueDefinition(psiEnumValueDefinition.getIdentifier().getText()));
                        }
                    }
                    final EnumTypeDefinition.Builder enumTypeDefinition = EnumTypeDefinition.newEnumTypeDefinition()
                            .name(psiEnumTypeDefinition.getNamedTypeDef().getName())
                            .enumValueDefinitions(enumValueDefinitions)
                            .sourceLocation(getSourceLocation(psiDefinition));
                    registry.add(enumTypeDefinition.build());
                }

            } else if (psiDefinition instanceof JSGraphQLEndpointUnionTypeDefinition) {

                final JSGraphQLEndpointUnionTypeDefinition psiUnionTypeDefinition = (JSGraphQLEndpointUnionTypeDefinition) psiDefinition;
                if (psiUnionTypeDefinition.getNamedTypeDef() != null) {

                    final List<Type> memberTypes = Lists.newArrayList();
                    if (psiUnionTypeDefinition.getUnionMemberSet() != null) {
                        for (JSGraphQLEndpointUnionMember psiUnionMember : psiUnionTypeDefinition.getUnionMemberSet().getUnionMemberList()) {
                            memberTypes.add(new TypeName(psiUnionMember.getIdentifier().getText()));
                        }
                    }
                    final UnionTypeDefinition.Builder builder = UnionTypeDefinition.newUnionTypeDefinition()
                            .name(psiUnionTypeDefinition.getNamedTypeDef().getName())
                            .memberTypes(memberTypes)
                            .sourceLocation(getSourceLocation(psiDefinition));
                    registry.add(builder.build());

                }

            } else if (psiDefinition instanceof JSGraphQLEndpointAnnotationDefinition) {

                final JSGraphQLEndpointAnnotationDefinition psiAnnotationDefinition = (JSGraphQLEndpointAnnotationDefinition) psiDefinition;
                if (psiAnnotationDefinition.getNamedTypeDef() != null) {
                    final List<InputValueDefinition> inputValueDefinitions = createInputValueDefinitions(psiAnnotationDefinition.getArgumentsDefinition(), errors);
                    final List<DirectiveLocation> directiveLocations = Lists.newArrayList();
                    // endpoint language currently doesn't have grammar support for valid locations, so allow all locations
                    for (Introspection.DirectiveLocation directiveLocation : Introspection.DirectiveLocation.values()) {
                        directiveLocations.add(new DirectiveLocation(directiveLocation.name()));
                    }
                    final DirectiveDefinition.Builder builder = DirectiveDefinition.newDirectiveDefinition()
                            .name(psiAnnotationDefinition.getNamedTypeDef().getName())
                            .inputValueDefinitions(inputValueDefinitions)
                            .directiveLocations(directiveLocations)
                            .sourceLocation(getSourceLocation(psiDefinition));
                    registry.add(builder.build());
                }

            } else if (psiDefinition instanceof JSGraphQLEndpointScalarTypeDefinition) {
                final JSGraphQLEndpointScalarTypeDefinition scalarTypeDefinition = (JSGraphQLEndpointScalarTypeDefinition) psiDefinition;
                final JSGraphQLEndpointNamedTypeDef scalarName = scalarTypeDefinition.getNamedTypeDef();
                if (scalarName != null) {
                    final SourceLocation sourceLocation = getSourceLocation(psiDefinition);
                    final Description description = getDescription(scalarTypeDefinition, sourceLocation);
                    registry.add(ScalarTypeDefinition.newScalarTypeDefinition().name(scalarName.getName()).description(description).sourceLocation(sourceLocation).build());
                }
            }
        });

        return new GraphQLRegistryInfo(registry, errors, !namedTypes.isEmpty());
    }

    private Description getDescription(JSGraphQLEndpointNamedTypeDefinition typeDefinition, SourceLocation sourceLocation) {
        if (typeDefinition.getNamedTypeDef() instanceof JSGraphQLEndpointDocumentationAware) {
            final String documentation = ((JSGraphQLEndpointDocumentationAware) typeDefinition.getNamedTypeDef()).getDocumentation(false);
            if (StringUtils.isNotBlank(documentation)) {
                return new Description(documentation, sourceLocation, true);
            }
        }
        return null;
    }

    private void addFieldDefinition(List<FieldDefinition> fieldDefinitions, Set<String> addedFieldNames, JSGraphQLEndpointFieldDefinition endpointFieldDefinition, List<GraphQLException> errors) {
        final JSGraphQLEndpointProperty property = endpointFieldDefinition.getProperty();
        final String fieldName = property.getName();
        if (endpointFieldDefinition.getCompositeType() != null) {
            final Type fieldType = createType(endpointFieldDefinition.getCompositeType());
            if (fieldType != null) {
                if (addedFieldNames.add(fieldName)) {
                    final SourceLocation sourceLocation = getSourceLocation(endpointFieldDefinition);
                    final FieldDefinition.Builder builder = FieldDefinition.newFieldDefinition()
                            .name(fieldName)
                            .type(fieldType)
                            .inputValueDefinitions(createInputValueDefinitions(endpointFieldDefinition.getArgumentsDefinition(), errors))
                            .sourceLocation(sourceLocation);
                    if (property instanceof JSGraphQLEndpointDocumentationAware) {
                        final String documentation = ((JSGraphQLEndpointDocumentationAware) property).getDocumentation(false);
                        if (StringUtils.isNotBlank(documentation)) {
                            builder.description(new Description(documentation, sourceLocation, true));
                        }
                    }
                    fieldDefinitions.add(builder.build());
                }
            } else {
                errors.add(new JSGraphQLEndpointSchemaError("Field '" + fieldName + "' has no valid output type", endpointFieldDefinition));
            }
        } else {
            errors.add(new JSGraphQLEndpointSchemaError("Field '" + fieldName + "' has no output type defined", endpointFieldDefinition));
        }
    }

    private List<InputValueDefinition> createInputValueDefinitions(JSGraphQLEndpointArgumentsDefinition argumentsDefinition, List<GraphQLException> errors) {
        if (argumentsDefinition != null && argumentsDefinition.getInputValueDefinitions() != null) {
            final List<InputValueDefinition> result = Lists.newArrayList();
            for (JSGraphQLEndpointInputValueDefinition psiArgument : argumentsDefinition.getInputValueDefinitions().getInputValueDefinitionList()) {
                final String argumentName = psiArgument.getInputValueDefinitionIdentifier().getIdentifier().getText();
                final JSGraphQLEndpointCompositeType psiCompositeType = psiArgument.getCompositeType();
                if (psiCompositeType != null) {
                    final Type type = createType(psiCompositeType);
                    if (type != null) {
                        result.add(new InputValueDefinition(argumentName, type));
                    } else {
                        errors.add(new JSGraphQLEndpointSchemaError("Unable to create schema type from '" + psiCompositeType.getText() + "' has no type", psiCompositeType));
                    }
                } else {
                    errors.add(new JSGraphQLEndpointSchemaError("Argument '" + argumentName + "' has no type", psiArgument));
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private Type createType(JSGraphQLEndpointCompositeType endpointCompositeType) {
        final boolean isNonNull = endpointCompositeType.getText().contains("!");
        if (endpointCompositeType.getListType() != null) {
            final JSGraphQLEndpointNamedType listElementType = endpointCompositeType.getListType().getNamedType();
            if (listElementType != null) {
                final String name = listElementType.getName();
                if (name != null) {
                    Type type = TypeName.newTypeName(name).sourceLocation(getSourceLocation(listElementType)).build();
                    type = ListType.newListType(type).sourceLocation(getSourceLocation(endpointCompositeType)).build();
                    if (isNonNull) {
                        type = NonNullType.newNonNullType(type).sourceLocation(getSourceLocation(endpointCompositeType)).build();
                    }
                    return type;
                }
            }
        } else if (endpointCompositeType.getNamedType() != null) {
            final String name = endpointCompositeType.getNamedType().getName();
            if (name != null) {
                Type type = TypeName.newTypeName(name).sourceLocation(getSourceLocation(endpointCompositeType.getNamedType())).build();
                if (isNonNull) {
                    type = NonNullType.newNonNullType(type).sourceLocation(getSourceLocation(endpointCompositeType)).build();
                }
                return type;
            }
        }
        return null;
    }

    private SourceLocation getSourceLocation(PsiElement psiSourceElement) {
        return new SourceLocation(-1, -1, psiSourceElement.getContainingFile().getName());
    }

    private Map<String, JSGraphQLNamedType> computeNamedTypes(PsiElement scopedPsiElement) {
        final GraphQLNamedScope schemaScope = getSchemaScope(scopedPsiElement);
        if (schemaScope == null) {
            return Collections.emptyMap();
        }
        return endpointTypesByName.computeIfAbsent(schemaScope, p -> {
            final Map<String, JSGraphQLNamedType> result = Maps.newConcurrentMap();
            final PsiFile entryPsiFile = getEndpointEntryPsiFile(scopedPsiElement);
            if (entryPsiFile != null) {
                Collection<JSGraphQLEndpointNamedTypeDefinition> endpointNamedTypeDefinitions = JSGraphQLEndpointPsiUtil.getKnownDefinitions(
                        entryPsiFile,
                        JSGraphQLEndpointNamedTypeDefinition.class,
                        true,
                        null
                );
                for (JSGraphQLEndpointNamedTypeDefinition typeDefinition : endpointNamedTypeDefinitions) {
                    if (typeDefinition.getNamedTypeDef() != null) {
                        final String typeName = typeDefinition.getNamedTypeDef().getText();
                        final JSGraphQLNamedType namedType = new JSGraphQLNamedType(typeDefinition, typeDefinition.getNamedTypeDef());
                        final JSGraphQLEndpointFieldDefinitionSet fieldDefinitionSet = PsiTreeUtil.findChildOfType(typeDefinition, JSGraphQLEndpointFieldDefinitionSet.class);
                        if (fieldDefinitionSet != null) {
                            final JSGraphQLEndpointFieldDefinition[] fields = PsiTreeUtil.getChildrenOfType(fieldDefinitionSet, JSGraphQLEndpointFieldDefinition.class);
                            if (fields != null) {
                                for (JSGraphQLEndpointFieldDefinition field : fields) {
                                    final JSGraphQLEndpointCompositeType propertyValueType = field.getCompositeType();
                                    if (propertyValueType != null) {
                                        String propertyValueTypeName = null;
                                        if (propertyValueType.getListType() != null) {
                                            final JSGraphQLEndpointNamedType listItemType = propertyValueType.getListType().getNamedType();
                                            if (listItemType != null) {
                                                propertyValueTypeName = listItemType.getText();
                                            }
                                        } else if (propertyValueType.getNamedType() != null) {
                                            propertyValueTypeName = propertyValueType.getNamedType().getText();
                                        }
                                        if (propertyValueTypeName != null) {
                                            namedType.properties.put(
                                                    field.getProperty().getText(),
                                                    new JSGraphQLPropertyType(field.getProperty(), namedType, propertyValueTypeName)
                                            );
                                        }
                                    }
                                }
                            }
                        }
                        result.put(typeName, namedType);
                        if ("Query".equals(typeName)) {
                            // also use Query for anonymous queries that are selection sets
                            result.put("SelectionSet", namedType);
                        }
                    }
                }
            }

            return result;

        });
    }

    @Override
    public void dispose() {
    }
}
