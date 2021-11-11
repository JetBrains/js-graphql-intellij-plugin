/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.completion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementWeigher;
import com.intellij.lang.jsgraphql.ide.documentation.GraphQLDocumentationMarkdownRenderer;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.ide.references.GraphQLResolveUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLObjectValueImpl;
import com.intellij.lang.jsgraphql.schema.*;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.types.validation.rules.VariablesTypesMatcher;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.StandardPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.lang.jsgraphql.ide.completion.GraphQLCompletionKeyword.*;
import static com.intellij.patterns.PlatformPatterns.psiComment;
import static com.intellij.patterns.PlatformPatterns.psiElement;

@SuppressWarnings("rawtypes")
public class GraphQLCompletionContributor extends CompletionContributor {

    // top level keywords (incomplete keywords such as "q" is inside error Psi element, hence superParent
    private static final PsiElementPattern.Capture<PsiElement> TOP_LEVEL_KEYWORD_PATTERN =
        psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLFile.class);

    // keyword after "extend" keyword
    private static final PsiElementPattern.Capture<PsiElement> EXTEND_KEYWORD_PATTERN =
        psiElement().afterLeaf(psiElement(GraphQLElementTypes.EXTEND_KEYWORD));

    public static final PsiElementPattern.Capture<PsiElement> TYPE_NAME_AFTER_COLON_PATTERN =
        psiElement(GraphQLElementTypes.NAME).afterLeafSkipping(
            // skip
            PlatformPatterns.or(psiComment(), psiElement(TokenType.WHITE_SPACE), psiElement(GraphQLElementTypes.BRACKET_L)),
            // until argument type colon occurs
            psiElement(GraphQLElementTypes.COLON)
        );

    private static final GraphQLCompletionKeyword[] TOP_LEVEL_KEYWORDS = {
        QUERY,
        SUBSCRIPTION,
        MUTATION,
        FRAGMENT,
        SCHEMA,
        SCALAR,
        TYPE,
        INTERFACE,
        INPUT,
        ENUM,
        UNION,
        DIRECTIVE,
        EXTEND
    };

    private static final GraphQLCompletionKeyword[] EXTEND_KEYWORDS = {
        SCALAR,
        TYPE,
        INTERFACE,
        INPUT,
        ENUM,
        UNION,
        SCHEMA,
    };

    private static final GraphQLCompletionKeyword[] OPERATION_NAME_KEYWORDS = {
        QUERY, MUTATION, SUBSCRIPTION
    };

    public GraphQLCompletionContributor() {

        // top level keywords
        completeTopLevelKeywords();

        // extend <completion>
        completeExtendFollowingKeyword();

        // extend (type|interface|scalar|union|enum|input) <completion>
        completeTypeNameToExtend();

        // field names inside selection sets of operations, fields, fragments
        completeFieldNames(); // TODO

        // on <completion of type name>
        completeFragmentOnTypeName(); // TODO

        // ...<completion of fragment name>
        completeSpreadFragmentName(); // TODO

        // completion on argument name in fields and directives
        completeArgumentName(); // TODO

        // completion on object value field
        completeObjectValueField(); // TODO

        // completion on directive name
        completeDirectiveName();

        // completion on directive location
        completeDirectiveLocation();

        // completion on variable definition type
        completeVariableDefinitionTypeName(); // TODO

        // completion of constants (true, false, null, enums) and '{}', '[]'
        completeConstantsOrListOrInputObject(); // TODO

        completeEnumNamesInList(); // TODO

        // completion of "implements" in type or type extension
        completeImplementsKeyword();

        // completion of interface name inside "implements"
        completeImplementsTypeName();

        // completion of field definition to implement interface fields
        completeFieldDefinitionFromImplementedInterface();

        // completion of union type members
        completeUnionMemberTypeName();

        // completion of field definition type
        completeFieldDefinitionTypes();

        // completion of input field definition and default value types
        completeInputFieldDefinitionAndArgumentInputTypes();

        // completion of operation type names in schema definition
        completeOperationTypeNamesInSchemaDefinition();

        // completion of operation types schema definition
        completionOfOperationKeywordsInSchemaDefinition();

        // completion of variable name referenced as an argument value
        completeVariableName(); // TODO
    }

    private void completionOfOperationKeywordsInSchemaDefinition() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLOperationTypeDefinition operationTypeDefinition = PsiTreeUtil.getParentOfType(completionElement,
                    GraphQLOperationTypeDefinition.class);
                if (operationTypeDefinition == null || operationTypeDefinition.getOperationType() != null) {
                    return;
                }
                final Set<String> keywords = ContainerUtil.map2Set(OPERATION_NAME_KEYWORDS, GraphQLCompletionKeyword::getText);
                Collection<GraphQLOperationTypeDefinition> existingDefinitions =
                    PsiTreeUtil.findChildrenOfType(operationTypeDefinition.getParent(), GraphQLOperationTypeDefinition.class);
                for (GraphQLOperationTypeDefinition existingDefinition : existingDefinitions) {
                    if (existingDefinition.getOperationType() != null) {
                        keywords.remove(existingDefinition.getOperationType().getText());
                    }
                }
                keywords.forEach(keyword -> result.addElement(GraphQLCompletionUtil.createOperationNameKeywordLookupElement(keyword)));
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).inside(GraphQLOperationTypeDefinition.class), provider);
    }

    private void completeOperationTypeNamesInSchemaDefinition() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                GraphQLDefinition definition = GraphQLResolveUtil.findContainingDefinition(completionElement);
                if (!(definition instanceof GraphQLSchemaDefinition) && !(definition instanceof GraphQLSchemaExtension)) {
                    return;
                }
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                final Collection<GraphQLTypeName> referencedTypes = PsiTreeUtil.findChildrenOfType(definition, GraphQLTypeName.class);
                final Set<String> currentTypeNames = referencedTypes.stream().map(PsiNamedElement::getName).collect(Collectors.toSet());
                registry.types().values().forEach(type -> {
                    if (isIgnoredType(type)) return;
                    if (type instanceof ObjectTypeDefinition && !currentTypeNames.contains(type.getName())) {
                        result.addElement(LookupElementBuilder.create(type.getName()));
                    }
                });
            }
        };
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).afterLeaf(":").inside(GraphQLOperationTypeDefinition.class),
            provider);
    }

    private void completeInputFieldDefinitionAndArgumentInputTypes() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                addInputTypeCompletions(result, registry);
            }
        };
        extend(CompletionType.BASIC,
            TYPE_NAME_AFTER_COLON_PATTERN.inside(
                false, psiElement(GraphQLInputValueDefinition.class), psiElement(GraphQLDefaultValue.class)),
            provider);
    }

    private void completeFieldDefinitionTypes() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                GraphQLSchemaInfo schemaInfo = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(completionElement);
                final GraphQLSchema schema = schemaInfo.getSchema();
                final TypeDefinitionRegistry registry = schemaInfo.getRegistryInfo().getTypeDefinitionRegistry();
                final Set<String> filteredTypes = GraphQLSchemaUtil.getSchemaOperationTypeNames(schema);
                registry.scalars().values().forEach(scalar ->
                    result.addElement(GraphQLCompletionUtil.createTypeNameLookupElement(scalar.getName())));
                registry.types().values().forEach(type -> {
                    if (isIgnoredType(type)) return;
                    if (!(type instanceof InputObjectTypeDefinition) && !filteredTypes.contains(type.getName())) {
                        result.addElement(GraphQLCompletionUtil.createTypeNameLookupElement(type.getName()));
                    }
                });
            }
        };
        extend(CompletionType.BASIC,
            StandardPatterns.and(
                TYPE_NAME_AFTER_COLON_PATTERN.inside(GraphQLFieldDefinition.class),
                StandardPatterns.not(psiElement().inside(GraphQLArgumentsDefinition.class)), // field def arguments
                StandardPatterns.not(psiElement().inside(GraphQLArguments.class)) // directive argument
            ), provider);
    }

    private void completeUnionMemberTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLUnionMembers unionMembers = PsiTreeUtil.getParentOfType(completionElement, GraphQLUnionMembers.class);
                if (unionMembers == null) {
                    return;
                }
                final Set<String> currentMembers = Sets.newHashSet();
                GraphQLSchemaInfo schemaInfo = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(completionElement);
                currentMembers.addAll(GraphQLSchemaUtil.getSchemaOperationTypeNames(schemaInfo.getSchema()));
                unionMembers.getTypeNameList().forEach(t -> currentMembers.add(t.getName()));
                final TypeDefinitionRegistry typeDefinitionRegistry = schemaInfo.getRegistryInfo().getTypeDefinitionRegistry();
                typeDefinitionRegistry.getTypes(ObjectTypeDefinition.class).forEach(schemaType -> {
                    String name = schemaType.getName();
                    if (!isIgnoredType(name) && currentMembers.add(name)) {
                        result.addElement(GraphQLCompletionUtil.createTypeNameLookupElement(name));
                    }
                });
            }
        };
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME)
                .inside(psiElement(GraphQLElementTypes.TYPE_NAME).inside(psiElement(GraphQLElementTypes.UNION_MEMBERS))),
            provider);
    }

    private void completeFieldDefinitionFromImplementedInterface() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLDefinition definition = GraphQLResolveUtil.findContainingDefinition(completionElement);
                if (definition == null) {
                    return;
                }
                final GraphQLImplementsInterfaces implementsInterfaces =
                    PsiTreeUtil.getChildOfType(definition, GraphQLImplementsInterfaces.class);
                if (implementsInterfaces == null) {
                    return;
                }
                final Set<String> currentFields = PsiTreeUtil.findChildrenOfType(definition, GraphQLFieldDefinition.class).stream()
                    .map(PsiNamedElement::getName).filter(Objects::nonNull).collect(Collectors.toSet());
                implementsInterfaces.getTypeNameList().forEach(interfaceTypeName -> {
                    final PsiElement typeDefinitionName = GraphQLResolveUtil.resolve(interfaceTypeName);
                    if (typeDefinitionName == null) {
                        return;
                    }
                    final Collection<GraphQLFieldDefinition> fieldDefinitions = PsiTreeUtil.findChildrenOfType(
                        GraphQLResolveUtil.findContainingDefinition(typeDefinitionName),
                        GraphQLFieldDefinition.class
                    );
                    for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                        if (currentFields.contains(fieldDefinition.getName())) {
                            continue;
                        }

                        final String implementedField = interfaceTypeName.getName() + "." + fieldDefinition.getName();
                        result.addElement(
                            GraphQLCompletionUtil.createFieldOverrideLookupElement(fieldDefinition.getText(), implementedField));
                    }
                });

            }
        };
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).withParent(psiElement(GraphQLIdentifier.class).withParent(GraphQLFieldDefinition.class)),
            provider);
    }

    private void completeImplementsTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLImplementsInterfaces implementsInterfaces =
                    PsiTreeUtil.getParentOfType(completionElement, GraphQLImplementsInterfaces.class);
                if (implementsInterfaces == null) {
                    return;
                }
                final Set<String> currentInterfaces = Sets.newHashSet();
                implementsInterfaces.getTypeNameList().forEach(t -> currentInterfaces.add(t.getName()));
                final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                typeDefinitionRegistry.getTypes(InterfaceTypeDefinition.class).forEach(schemaInterface -> {
                    if (currentInterfaces.add(schemaInterface.getName())) {
                        result.addElement(GraphQLCompletionUtil.createTypeNameLookupElement(schemaInterface.getName()));
                    }
                });
            }
        };
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME)
                .inside(psiElement(GraphQLElementTypes.TYPE_NAME).inside(psiElement(GraphQLElementTypes.IMPLEMENTS_INTERFACES))),
            provider);
    }

    private void completeImplementsKeyword() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                // TODO: [vepanimas] extract a common interface for implementing types
                final GraphQLTypeSystemDefinition typeDefinition = PsiTreeUtil.getParentOfType(completionElement,
                    GraphQLObjectTypeDefinition.class, GraphQLObjectTypeExtensionDefinition.class, GraphQLInterfaceTypeDefinition.class,
                    GraphQLInterfaceTypeExtensionDefinition.class);
                if (typeDefinition instanceof GraphQLObjectTypeDefinition &&
                    ((GraphQLObjectTypeDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                if (typeDefinition instanceof GraphQLObjectTypeExtensionDefinition &&
                    ((GraphQLObjectTypeExtensionDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeDefinition &&
                    ((GraphQLInterfaceTypeDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeExtensionDefinition &&
                    ((GraphQLInterfaceTypeExtensionDefinition) typeDefinition).getImplementsInterfaces() != null) {
                    return;
                }
                result.addElement(GraphQLCompletionUtil.createKeywordLookupElement(IMPLEMENTS));
            }
        };
        final ElementPattern<PsiElement> insideTypeDefElement = PlatformPatterns.or(
            psiElement().inside(GraphQLObjectTypeDefinition.class),
            psiElement().inside(GraphQLObjectTypeExtensionDefinition.class),
            psiElement().inside(GraphQLInterfaceTypeDefinition.class),
            psiElement().inside(GraphQLInterfaceTypeExtensionDefinition.class)
        );
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).afterLeaf(psiElement(GraphQLElementTypes.NAME).inside(insideTypeDefElement)),
            provider);
    }

    private void completeConstantsOrListOrInputObject() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement,
                    GraphQLTypeScopeProvider.class);
                if (typeScopeProvider != null) {
                    final GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        final InsertHandler<LookupElement> literalInsertHandler = (ctx, item) -> {
                            ctx.getEditor().getCaretModel().moveCaretRelatively(-1, 0, false, false, false);
                            AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
                        };
                        if (typeScope instanceof GraphQLList || (typeScope instanceof GraphQLNonNull && ((GraphQLNonNull) typeScope).getWrappedType() instanceof GraphQLList)) {
                            // list or non-null list
                            result.addElement(LookupElementBuilder.create("[]").withInsertHandler(literalInsertHandler));
                        } else {
                            // raw type is enum, boolean or object
                            final GraphQLUnmodifiedType rawType = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                            if (rawType instanceof GraphQLEnumType) {
                                ((GraphQLEnumType) rawType).getValues().forEach(value -> result.addElement(
                                    LookupElementBuilder.create(value.getName()).withTypeText(GraphQLSchemaUtil.getTypeName(typeScope))));
                            } else if (rawType instanceof GraphQLInputObjectType) {
                                if (parameters.getOriginalPosition() != null && !parameters.getOriginalPosition().getText().equals("{")) {
                                    result.addElement(LookupElementBuilder.create("{}").withInsertHandler(literalInsertHandler));
                                }
                            } else {
                                if ("Boolean".equals(rawType.getName())) {
                                    result.addElement(LookupElementBuilder.create("true").withBoldness(true));
                                    result.addElement(LookupElementBuilder.create("false").withBoldness(true));
                                }
                                // TODO JKM 'null' completion?
                            }
                        }
                    }
                }

            }
        };
        // NOTE: the PSI produces enum values when none of the keywords match, e.g. 'tru' is considered a possible enum value
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).afterLeaf(":").withSuperParent(2, GraphQLEnumValue.class),
            provider);
    }

    private void completeEnumNamesInList() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement,
                    GraphQLTypeScopeProvider.class);
                if (typeScopeProvider != null) {
                    final GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        final GraphQLUnmodifiedType rawType = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        if (rawType instanceof GraphQLEnumType) {
                            ((GraphQLEnumType) rawType).getValues().forEach(
                                value -> result.addElement(LookupElementBuilder.create(value.getName()).withTypeText(rawType.getName())));
                        }
                    }
                }

            }
        };
        // NOTE: the PSI produces enum values when none of the keywords match, e.g. 'tru' is considered a possible enum value
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).withSuperParent(2, psiElement(GraphQLEnumValue.class).withParent(GraphQLArrayValue.class)),
            provider);
    }

    private void completeVariableDefinitionTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                addInputTypeCompletions(result, registry);
            }
        };
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).inside(GraphQLTypeName.class).inside(GraphQLVariableDefinition.class),
            provider);
    }

    private void addInputTypeCompletions(@NotNull CompletionResultSet result, TypeDefinitionRegistry registry) {
        if (registry != null) {
            registry.scalars().values().forEach(scalar -> result.addElement(LookupElementBuilder.create(scalar.getName())));
            registry.types().values().forEach(type -> {
                if (isIgnoredType(type)) return;
                if (type instanceof EnumTypeDefinition || type instanceof InputObjectTypeDefinition) {
                    result.addElement(LookupElementBuilder.create(type.getName()));
                }
            });
        }
    }

    private void completeDirectiveLocation() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLDirectiveLocations directiveLocations =
                    PsiTreeUtil.getParentOfType(completionElement, GraphQLDirectiveLocations.class);
                if (directiveLocations == null) {
                    return;
                }

                PsiElement prevSibling = PsiTreeUtil.skipWhitespacesAndCommentsBackward(directiveLocations);
                if (prevSibling instanceof PsiErrorElement) {
                    List<GraphQLCompletionKeyword> keywords = new SmartList<>(ON);
                    if (PsiUtilCore.getElementType(prevSibling.getPrevSibling()) != GraphQLElementTypes.REPEATABLE_KEYWORD) {
                        keywords.add(REPEATABLE);
                    }

                    for (GraphQLCompletionKeyword keyword : keywords) {
                        result.addElement(GraphQLCompletionUtil.createKeywordLookupElement(keyword));
                    }
                    return; // no need to complete locations
                }

                final Set<String> currentLocations = Sets.newHashSet();
                directiveLocations.getDirectiveLocationList().forEach(location -> currentLocations.add(location.getText()));
                GraphQLFile builtInSchema = ContainerUtil.getFirstItem(
                    GraphQLResolveUtil.getLibraryFiles(GraphQLLibraryTypes.SPECIFICATION, completionElement));
                if (builtInSchema == null) {
                    return;
                }

                List<GraphQLTypeSystemDefinition> definitions = ContainerUtil.filter(
                    builtInSchema.getTypeDefinitions(),
                    t -> t instanceof GraphQLEnumTypeDefinition);
                for (GraphQLDefinition definition : definitions) {
                    GraphQLEnumTypeDefinition enumTypeDefinition = (GraphQLEnumTypeDefinition) definition;
                    final GraphQLTypeNameDefinition enumTypeName = enumTypeDefinition.getTypeNameDefinition();
                    if (enumTypeName == null || !GraphQLKnownTypes.INTROSPECTION_DIRECTIVE_LOCATION.equals(enumTypeName.getName())) {
                        continue;
                    }

                    final GraphQLEnumValueDefinitions enumValueDefinitions = enumTypeDefinition.getEnumValueDefinitions();
                    if (enumValueDefinitions != null) {
                        enumValueDefinitions.getEnumValueDefinitionList().forEach(value -> {
                            final String locationName = value.getEnumValue().getName();
                            if (locationName != null && currentLocations.add(locationName)) {
                                result.addElement(GraphQLCompletionUtil.createDirectiveLocationLookupElement(locationName));
                            }
                        });
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withParent(GraphQLDirectiveLocation.class), provider);
    }

    private void completeDirectiveName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                final TypeDefinitionRegistry registry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(completionElement).getTypeDefinitionRegistry();

                for (DirectiveDefinition directiveDefinition : registry.getDirectiveDefinitions().values()) {
                    final EnumSet<Introspection.DirectiveLocation> validLocations = EnumSet.noneOf(Introspection.DirectiveLocation.class);
                    for (DirectiveLocation directiveLocation : directiveDefinition.getDirectiveLocations()) {
                        try {
                            validLocations.add(Introspection.DirectiveLocation.valueOf(directiveLocation.getName()));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    if (!isValidDirectiveLocation(validLocations, parameters.getPosition())) {
                        continue;
                    }
                    boolean hasRequiredArgs = false;
                    for (InputValueDefinition directiveArgument : directiveDefinition.getInputValueDefinitions()) {
                        if (directiveArgument.getType() instanceof NonNullType) {
                            hasRequiredArgs = true;
                            break;
                        }
                    }

                    result.addElement(
                        GraphQLCompletionUtil.createDirectiveNameLookupElement(directiveDefinition.getName(), hasRequiredArgs));
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLDirective.class), provider);
    }

    private void completeObjectValueField() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement,
                    GraphQLObjectValueImpl.class);
                if (typeScopeProvider != null) {
                    GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        // unwrap lists, non-null etc:
                        typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        if (typeScope instanceof GraphQLInputFieldsContainer) {
                            final List<GraphQLInputObjectField> fieldDefinitions = ((GraphQLInputFieldsContainer) typeScope).getFieldDefinitions();
                            final GraphQLObjectValue objectValue = PsiTreeUtil.getParentOfType(completionElement, GraphQLObjectValue.class);
                            if (objectValue != null) {
                                // get the existing object field names to filter them out
                                final Set<String> existingFieldNames = objectValue.getObjectFieldList().stream().map(
                                    PsiNamedElement::getName).collect(Collectors.toSet());
                                for (GraphQLInputObjectField fieldDefinition : fieldDefinitions) {
                                    if (!existingFieldNames.contains(fieldDefinition.getName())) {
                                        LookupElementBuilder element = LookupElementBuilder.create(fieldDefinition.getName()).withTypeText(
                                            GraphQLSchemaUtil.typeString(fieldDefinition.getType()));
                                        if (fieldDefinition.getDescription() != null) {
                                            final String fieldDocumentation = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(
                                                fieldDefinition.getDescription(), true);
                                            element = element.withTailText(" - " + fieldDocumentation, true);
                                        }
                                        result.addElement(element.withInsertHandler(AddColonSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                                    }
                                }
                            }
                        }
                    }
                }

            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLObjectField.class), provider);
    }

    private void completeArgumentName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());
                final GraphQLNamedElement element =
                    PsiTreeUtil.getParentOfType(completionElement, GraphQLField.class, GraphQLDirective.class);
                if (element == null || element.getName() == null) {
                    return;
                }

                if (element instanceof GraphQLDirective) {
                    addDirectiveArguments(result, completionElement, ((GraphQLDirective) element));
                } else if (element instanceof GraphQLField) {
                    addFieldArguments(result, ((GraphQLField) element));
                }
            }

            private void addDirectiveArguments(@NotNull CompletionResultSet result,
                                               @NotNull PsiElement completionElement,
                                               @NotNull GraphQLDirective directive) {
                final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(completionElement).getSchema();
                com.intellij.lang.jsgraphql.types.schema.GraphQLDirective directiveDefinition =
                    schema.getFirstDirective(directive.getName());
                if (directiveDefinition == null) {
                    return;
                }
                final Set<String> existingArgumentNames = Sets.newHashSet();
                GraphQLArguments arguments = directive.getArguments();
                if (arguments != null) {
                    for (GraphQLArgument directiveArgument : arguments.getArgumentList()) {
                        ContainerUtil.addIfNotNull(existingArgumentNames, directiveArgument.getName());
                    }
                }
                addArgumentResults(result, directiveDefinition.getArguments(), existingArgumentNames);
            }

            private void addFieldArguments(@NotNull CompletionResultSet result, @NotNull GraphQLField field) {
                final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
                if (typeScopeProvider == null) {
                    return;
                }
                GraphQLType typeScope = typeScopeProvider.getTypeScope();
                if (typeScope != null) {
                    // we need the raw type to get the fields
                    typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                }
                if (!(typeScope instanceof GraphQLFieldsContainer)) {
                    return;
                }
                final com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition fieldDefinition =
                    ((GraphQLFieldsContainer) typeScope).getFieldDefinition(field.getName());
                if (fieldDefinition == null || field.getArguments() == null) {
                    return;
                }
                final Set<String> existingArgumentNames = field.getArguments().getArgumentList().stream()
                    .map(PsiNamedElement::getName).filter(Objects::nonNull).collect(Collectors.toSet());
                addArgumentResults(result, fieldDefinition.getArguments(), existingArgumentNames);
            }

            private void addArgumentResults(@NotNull CompletionResultSet result,
                                            @NotNull List<com.intellij.lang.jsgraphql.types.schema.GraphQLArgument> arguments,
                                            @NotNull Set<String> existingArgumentNames) {
                for (com.intellij.lang.jsgraphql.types.schema.GraphQLArgument graphQLArgument : arguments) {
                    String name = graphQLArgument.getName();
                    if (name != null && existingArgumentNames.add(name)) {
                        String typeText = GraphQLSchemaUtil.typeString(graphQLArgument.getType());
                        result.addElement(GraphQLCompletionUtil.createArgumentNameLookupElement(name, typeText));
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLArgument.class), provider);
    }

    private void completeSpreadFragmentName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());

                final GraphQLTypeScopeProvider typeScopeProvider =
                    PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);

                if (typeScopeProvider != null) {
                    GraphQLType typeScope = typeScopeProvider.getTypeScope();
                    if (typeScope != null) {
                        // unwrap non-nulls, lists etc. since we want the raw type to match with the fragment type conditions
                        typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);

                        // fragment must be compatible with the type in scope
                        final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(
                                completionElement.getProject())
                            .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();

                        final List<GraphQLFragmentDefinition> knownFragmentDefinitions = GraphQLPsiSearchHelper.getInstance(
                            completionElement.getProject()).getKnownFragmentDefinitions(parameters.getOriginalFile());
                        for (GraphQLFragmentDefinition fragmentDefinition : knownFragmentDefinitions) {
                            final String name = fragmentDefinition.getName();
                            if (name != null) {
                                // suggest compatible fragments based on type type conditions
                                if (isFragmentApplicableInTypeScope(typeDefinitionRegistry, fragmentDefinition, typeScope)) {
                                    result.addElement(LookupElementBuilder.create(name));
                                }
                            }
                        }

                    }

                }

                // the on keyword for inline fragments
                final PsiElement beforeCompletion = PsiTreeUtil.prevLeaf(parameters.getPosition());
                final boolean addSpaceBefore = beforeCompletion != null && beforeCompletion.getNode().getElementType() == GraphQLElementTypes.SPREAD;
                final String onCompletion = addSpaceBefore ? " on" : "on";

                result.addElement(LookupElementBuilder.create(onCompletion).withPresentableText("on").withBoldness(true).withInsertHandler(
                    AddSpaceInsertHandler.INSTANCE_WITH_AUTO_POPUP));

            }
        };
        extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement(GraphQLElementTypes.SPREAD)), provider);
    }

    private void completeFragmentOnTypeName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                final PsiElement completionElement = parameters.getPosition();

                // the type condition that the 'on' keyword belongs to
                GraphQLTypeCondition typeCondition = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeCondition.class);
                if (typeCondition == null) {
                    // typeCondition is on the left if the selection set follows
                    typeCondition = PsiTreeUtil.getPrevSiblingOfType(completionElement, GraphQLTypeCondition.class);
                }
                final boolean fragmentDefinition = typeCondition != null && typeCondition.getParent() instanceof GraphQLFragmentDefinition;

                final GraphQLSchemaProvider schemaProvider = GraphQLSchemaProvider.getInstance(completionElement.getProject());
                final TypeDefinitionRegistry typeDefinitionRegistry = schemaProvider
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();

                final List<Pair<TypeDefinition, Description>> fragmentTypes = Lists.newArrayList();

                if (fragmentDefinition) {
                    // completion in a top-level fragment definition, so add all known types, interfaces, unions
                    typeDefinitionRegistry.types().forEach((key, value) -> {
                        final boolean canFragment = value instanceof ObjectTypeDefinition || value instanceof UnionTypeDefinition || value instanceof InterfaceTypeDefinition;
                        if (canFragment) {
                            fragmentTypes.add(Pair.create(value, GraphQLTypeDefinitionUtil.getTypeDefinitionDescription(value)));
                        }
                    });
                } else {

                    // inline fragment, so get type scope
                    GraphQLTypeScopeProvider typeScopeProvider =
                        PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);

                    if (typeScopeProvider instanceof GraphQLInlineFragment && ((GraphQLInlineFragment) typeScopeProvider).getTypeCondition() == typeCondition) {
                        // if the type condition belongs to the type scope provider, we want the parent scope since that
                        // is the real source of what we can fragment on
                        typeScopeProvider = PsiTreeUtil.getParentOfType(typeScopeProvider, GraphQLTypeScopeProvider.class);
                    }

                    GraphQLType rawTypeScope = typeScopeProvider != null ? typeScopeProvider.getTypeScope() : null;
                    if (rawTypeScope != null) {
                        GraphQLUnmodifiedType typeScope = GraphQLSchemaUtil.getUnmodifiedType(
                            rawTypeScope); // unwrap non-null and lists since fragments are about the raw type
                        final TypeDefinition fragmentType = typeDefinitionRegistry.getType(typeScope.getName()).orElse(null);
                        if (fragmentType != null) {
                            final Ref<Consumer<TypeDefinition<?>>> addTypesRecursive = new Ref<>();
                            final Consumer<TypeDefinition<?>> addTypes = (typeToFragmentOn) -> {
                                if (typeToFragmentOn instanceof ObjectTypeDefinition) {
                                    fragmentTypes.add(
                                        Pair.create(typeToFragmentOn, GraphQLTypeDefinitionUtil.getTypeDefinitionDescription(typeToFragmentOn)));
                                    final List<Type> anImplements = ((ObjectTypeDefinition) typeToFragmentOn).getImplements();
                                    if (anImplements != null) {
                                        anImplements.forEach(type -> {
                                            final TypeDefinition typeDefinition = typeDefinitionRegistry.getType(type).orElse(null);
                                            if (typeDefinition instanceof InterfaceTypeDefinition) {
                                                fragmentTypes.add(Pair.create(typeDefinition,
                                                    GraphQLTypeDefinitionUtil.getTypeDefinitionDescription(typeDefinition)));
                                            }
                                        });
                                    }
                                } else if (typeToFragmentOn instanceof InterfaceTypeDefinition) {
                                    fragmentTypes.add(
                                        Pair.create(typeToFragmentOn, GraphQLTypeDefinitionUtil.getTypeDefinitionDescription(typeToFragmentOn)));
                                    final List<ObjectTypeDefinition> implementationsOf = typeDefinitionRegistry.getImplementationsOf(
                                        (InterfaceTypeDefinition) typeToFragmentOn);
                                    implementationsOf.forEach(
                                        impl -> fragmentTypes.add(Pair.create(impl, GraphQLTypeDefinitionUtil.getTypeDefinitionDescription(impl))));
                                } else if (typeToFragmentOn instanceof UnionTypeDefinition) {
                                    final List<Type> memberTypes = ((UnionTypeDefinition) typeToFragmentOn).getMemberTypes();
                                    if (memberTypes != null) {
                                        memberTypes.forEach(memberType -> typeDefinitionRegistry.getType(memberType).ifPresent(
                                            memberTypeDefinition -> addTypesRecursive.get().consume(memberTypeDefinition)));
                                    }
                                }
                            };
                            addTypesRecursive.set(addTypes);
                            addTypes.consume(fragmentType);
                        }
                    }

                }

                fragmentTypes.forEach(fragmentType -> {
                    String typeName = fragmentType.first.getName();
                    if (isIgnoredType(typeName)) return;

                    LookupElementBuilder element = LookupElementBuilder
                        .create(typeName)
                        .withBoldness(true);
                    if (fragmentType.second != null) {
                        final String documentation = GraphQLDocumentationMarkdownRenderer
                            .getDescriptionAsPlainText(fragmentType.second.getContent(), true);
                        element = element.withTailText(" - " + documentation, true);
                    }
                    result.addElement(element);
                });

            }
        };
        extend(CompletionType.BASIC, psiElement().afterLeaf(psiElement(GraphQLElementTypes.ON_KEYWORD)), provider);
    }

    private void completeFieldNames() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {

                // move "__*" fields to bottom
                final CompletionResultSet orderedResult = updateResult(parameters, result);

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());

                GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);

                // check for incomplete field name, in which case we want the parent element as type scope to find this field name
                GraphQLField completionField = null;
                if (completionElement.getNode().getElementType() == GraphQLElementTypes.NAME) {
                    completionField = PsiTreeUtil.getParentOfType(completionElement, GraphQLField.class);
                }
                if (typeScopeProvider == completionField) {
                    // completed on incomplete field name, use parent
                    typeScopeProvider = PsiTreeUtil.getParentOfType(typeScopeProvider, GraphQLTypeScopeProvider.class);
                }

                if (typeScopeProvider != null) {

                    PsiElement textBeforeCompletion = PsiTreeUtil.prevLeaf(completionElement);
                    boolean isSpread = textBeforeCompletion != null && textBeforeCompletion.getText().startsWith(".");

                    if (!isSpread) {
                        GraphQLType typeScope = typeScopeProvider.getTypeScope();
                        if (typeScope != null) {
                            // we need the raw type to get the fields
                            typeScope = GraphQLSchemaUtil.getUnmodifiedType(typeScope);
                        }
                        if (typeScope instanceof GraphQLFieldsContainer) {
                            ((GraphQLFieldsContainer) typeScope).getFieldDefinitions().forEach(field -> {
                                LookupElementBuilder element = LookupElementBuilder
                                    .create(field.getName())
                                    .withBoldness(true)
                                    .withTypeText(GraphQLSchemaUtil.typeString(field.getType()));
                                if (field.getDescription() != null) {
                                    final String fieldDocumentation = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(
                                        field.getDescription(), true);
                                    element = element.withTailText(" - " + fieldDocumentation, true);
                                }
                                if (field.isDeprecated()) {
                                    element = element.strikeout();
                                    if (field.getDeprecationReason() != null) {
                                        final String deprecationReason = GraphQLDocumentationMarkdownRenderer.getDescriptionAsPlainText(
                                            field.getDeprecationReason(), true);
                                        element = element.withTailText(" - Deprecated: " + deprecationReason, true);
                                    }
                                }
                                for (com.intellij.lang.jsgraphql.types.schema.GraphQLArgument fieldArgument : field.getArguments()) {
                                    if (fieldArgument.getType() instanceof GraphQLNonNull) {
                                        // on of the field arguments are required, so add the '()' for arguments
                                        element = element.withInsertHandler((ctx, item) -> {
                                            ParenthesesInsertHandler.WITH_PARAMETERS.handleInsert(ctx, item);
                                            AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(), null);
                                        });
                                        break;
                                    }
                                }
                                orderedResult.addElement(element);
                            });
                        }
                        if (!(typeScopeProvider instanceof GraphQLOperationDefinition)) {
                            // show the '...' except when top level selection in an operation
                            orderedResult.addElement(LookupElementBuilder.create("...").withInsertHandler(
                                (ctx, item) -> AutoPopupController.getInstance(ctx.getProject()).autoPopupMemberLookup(ctx.getEditor(),
                                    null)));
                            // and add the built-in __typename option
                            orderedResult.addElement(LookupElementBuilder.create("__typename"));
                        }
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement(GraphQLElementTypes.NAME).withSuperParent(2, GraphQLField.class), provider);
    }

    private void completeTypeNameToExtend() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeExtension typeExtension = PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeExtension.class);
                if (typeExtension == null) {
                    return;
                }
                final TypeDefinitionRegistry typeDefinitionRegistry = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getRegistryInfo(parameters.getOriginalFile()).getTypeDefinitionRegistry();
                final List<TypeDefinition<?>> types = Lists.newArrayList();
                if (typeExtension instanceof GraphQLScalarTypeExtensionDefinition) {
                    // scalars aren't fully fledged types in the registry
                    types.addAll(typeDefinitionRegistry.scalars().values());
                } else {
                    // "real" types
                    Class<? extends TypeDefinition<?>> applicableTypes = null;
                    if (typeExtension instanceof GraphQLObjectTypeExtensionDefinition) {
                        applicableTypes = ObjectTypeDefinition.class;
                    } else if (typeExtension instanceof GraphQLInterfaceTypeExtensionDefinition) {
                        applicableTypes = InterfaceTypeDefinition.class;
                    } else if (typeExtension instanceof GraphQLUnionTypeExtensionDefinition) {
                        applicableTypes = UnionTypeDefinition.class;
                    } else if (typeExtension instanceof GraphQLEnumTypeExtensionDefinition) {
                        applicableTypes = EnumTypeDefinition.class;
                    } else if (typeExtension instanceof GraphQLInputObjectTypeExtensionDefinition) {
                        applicableTypes = InputObjectTypeDefinition.class;
                    }
                    if (applicableTypes != null) {
                        types.addAll(typeDefinitionRegistry.getTypes(applicableTypes));
                    }
                }
                types.forEach(type -> {
                    if (isIgnoredType(type)) return;
                    result.addElement(GraphQLCompletionUtil.createExtendTypeNameLookupElement(type.getName()));
                });
            }
        };
        final ElementPattern<PsiElement> extendKeywords = PlatformPatterns.or(
            psiElement(GraphQLElementTypes.TYPE_KEYWORD),
            psiElement(GraphQLElementTypes.INTERFACE_KEYWORD),
            psiElement(GraphQLElementTypes.SCALAR_KEYWORD),
            psiElement(GraphQLElementTypes.UNION_KEYWORD),
            psiElement(GraphQLElementTypes.ENUM_KEYWORD),
            psiElement(GraphQLElementTypes.INPUT_KEYWORD)
        );
        extend(CompletionType.BASIC,
            psiElement(GraphQLElementTypes.NAME).inside(GraphQLTypeExtension.class).afterLeaf(extendKeywords),
            provider);
    }

    private void completeExtendFollowingKeyword() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                for (GraphQLCompletionKeyword keyword : EXTEND_KEYWORDS) {
                    result.addElement(GraphQLCompletionUtil.createKeywordLookupElement(keyword));
                }
            }
        };
        extend(CompletionType.BASIC, EXTEND_KEYWORD_PATTERN, provider);
    }

    private void completeTopLevelKeywords() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                Document document = parameters.getEditor().getDocument();
                int line = document.getLineNumber(parameters.getOffset());
                PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(parameters.getPosition());
                if (prevVisibleLeaf != null) {
                    // NOTE: "type Foo <completion>" would grammatically allow a new definition to follow immediately
                    // but this completion at that position is likely to be unexpected and would interfere with "implements" on types
                    // so we expect top level keywords to be the first visible element on the line to complete
                    if (line == document.getLineNumber(prevVisibleLeaf.getTextRange().getStartOffset())) {
                        return;
                    }
                }

                for (GraphQLCompletionKeyword keyword : TOP_LEVEL_KEYWORDS) {
                    result.addElement(GraphQLCompletionUtil.createKeywordLookupElement(keyword));
                }
            }
        };
        extend(CompletionType.BASIC, TOP_LEVEL_KEYWORD_PATTERN, provider);
    }

    private void completeVariableName() {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters,
                                          @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                final PsiElement completionElement = parameters.getPosition();
                final GraphQLTypeScopeProvider typeScopeProvider =
                    PsiTreeUtil.getParentOfType(completionElement, GraphQLTypeScopeProvider.class);
                if (typeScopeProvider == null) {
                    return;
                }

                final GraphQLType typeScope = typeScopeProvider.getTypeScope();
                if (typeScope == null) {
                    return;
                }

                final GraphQLTypedOperationDefinition query = PsiTreeUtil.getParentOfType(completionElement,
                    GraphQLTypedOperationDefinition.class);
                if (query == null || query.getVariableDefinitions() == null) {
                    return;
                }
                final VariablesTypesMatcher variablesTypesMatcher = new VariablesTypesMatcher();
                final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(completionElement.getProject())
                    .getSchemaInfo(parameters.getOriginalFile()).getSchema();

                for (GraphQLVariableDefinition variableDefinition : query.getVariableDefinitions().getVariableDefinitions()) {
                    if (variableDefinition.getType() == null) {
                        continue;
                    }

                    final GraphQLIdentifier variableTypeIdentifier = PsiTreeUtil.findChildOfType(variableDefinition.getType(),
                        GraphQLIdentifier.class);
                    if (variableTypeIdentifier == null) {
                        continue;
                    }
                    GraphQLType declaredType = schema.getType(variableTypeIdentifier.getText());
                    if (declaredType == null) {
                        continue;
                    }
                    if (variableTypeIdentifier.getNextSibling() instanceof LeafPsiElement && "!".equals(
                        variableTypeIdentifier.getNextSibling().getText())) {
                        declaredType = new GraphQLNonNull(declaredType);
                    }
                    PsiElement parent = variableTypeIdentifier.getParent();
                    while (parent != null && !(parent instanceof GraphQLVariableDefinition)) {
                        if (parent instanceof GraphQLListType) {
                            declaredType = new GraphQLList(declaredType);
                        } else if (parent instanceof GraphQLNonNullType) {
                            declaredType = new GraphQLNonNull(declaredType);
                        }
                        parent = parent.getParent();
                    }

                    final String variableNameCompletion = variableDefinition.getVariable().getText();
                    if (variablesTypesMatcher.doesVariableTypesMatch(declaredType, null, typeScope)) {
                        result.addElement(LookupElementBuilder.create(variableNameCompletion)
                            .withTypeText(GraphQLSchemaUtil.typeString(declaredType)));
                    } else {
                        PsiElement elementParent = completionElement.getParent();
                        PsiElement elementGrandParent = elementParent.getParent();
                        if ((elementParent instanceof GraphQLVariable && elementGrandParent instanceof GraphQLArrayValue) ||
                            (elementParent instanceof GraphQLIdentifier && elementGrandParent instanceof GraphQLEnumValue &&
                                elementGrandParent.getParent() instanceof GraphQLArrayValue)) {
                            // variable is used inside a list literal, e.g. [$myVar], so need to unwrap the type scope
                            GraphQLType typeScopeWithoutList = GraphQLSchemaUtil.unwrapListType(typeScope);
                            if (variablesTypesMatcher.doesVariableTypesMatch(declaredType, null, typeScopeWithoutList)) {
                                result.addElement(LookupElementBuilder.create(variableNameCompletion)
                                    .withTypeText(GraphQLSchemaUtil.typeString(declaredType)));
                            }
                        }
                    }
                }
            }
        };
        extend(CompletionType.BASIC, psiElement().andOr(
            psiElement(GraphQLElementTypes.VARIABLE_NAME),
            psiElement(GraphQLElementTypes.NAME).inside(GraphQLEnumValue.class)
        ).inside(GraphQLArgument.class), provider);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidDirectiveLocation(@NotNull EnumSet<Introspection.DirectiveLocation> validLocations,
                                             @Nullable PsiElement completionPosition) {
        final GraphQLDirectivesAware directivesAware = PsiTreeUtil.getParentOfType(completionPosition, GraphQLDirectivesAware.class);
        if (directivesAware == null) {
            return false;
        }
        for (Introspection.DirectiveLocation directiveLocation : validLocations) {
            if (isValidDirectiveLocation(directivesAware, directiveLocation)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidDirectiveLocation(@NotNull GraphQLDirectivesAware directivesAware,
                                             @NotNull Introspection.DirectiveLocation directiveLocation) {
        switch (directiveLocation) {
            // Executable locations
            case QUERY:
                if (directivesAware instanceof GraphQLTypedOperationDefinition) {
                    GraphQLOperationType type = ((GraphQLTypedOperationDefinition) directivesAware).getOperationType();
                    return type.getNode().findChildByType(GraphQLElementTypes.QUERY_KEYWORD) != null;
                }
                break;
            case MUTATION:
                if (directivesAware instanceof GraphQLTypedOperationDefinition) {
                    GraphQLOperationType type = ((GraphQLTypedOperationDefinition) directivesAware).getOperationType();
                    return type.getNode().findChildByType(GraphQLElementTypes.MUTATION_KEYWORD) != null;
                }
                break;
            case SUBSCRIPTION:
                if (directivesAware instanceof GraphQLTypedOperationDefinition) {
                    GraphQLOperationType type = ((GraphQLTypedOperationDefinition) directivesAware).getOperationType();
                    return type.getNode().findChildByType(GraphQLElementTypes.SUBSCRIPTION_KEYWORD) != null;
                }
                break;
            case FIELD:
                return directivesAware instanceof GraphQLField;
            case FRAGMENT_DEFINITION:
                return directivesAware instanceof GraphQLFragmentDefinition;
            case FRAGMENT_SPREAD:
                return directivesAware instanceof GraphQLFragmentSpread;
            case INLINE_FRAGMENT:
                return directivesAware instanceof GraphQLInlineFragment;

            // SDL
            case SCHEMA:
                return directivesAware instanceof GraphQLSchemaDefinition || directivesAware instanceof GraphQLSchemaExtension;
            case SCALAR:
                return directivesAware instanceof GraphQLScalarTypeDefinition || directivesAware instanceof GraphQLScalarTypeExtensionDefinition;
            case OBJECT:
                return directivesAware instanceof GraphQLObjectTypeDefinition || directivesAware instanceof GraphQLObjectTypeExtensionDefinition;
            case FIELD_DEFINITION:
                return directivesAware instanceof GraphQLFieldDefinition;
            case ARGUMENT_DEFINITION:
                return directivesAware instanceof GraphQLInputValueDefinition && directivesAware.getParent() instanceof GraphQLArgumentsDefinition;
            case INTERFACE:
                return directivesAware instanceof GraphQLInterfaceTypeDefinition || directivesAware instanceof GraphQLInterfaceTypeExtensionDefinition;
            case UNION:
                return directivesAware instanceof GraphQLUnionTypeDefinition || directivesAware instanceof GraphQLUnionTypeExtensionDefinition;
            case ENUM:
                return directivesAware instanceof GraphQLEnumTypeDefinition || directivesAware instanceof GraphQLEnumTypeExtensionDefinition;
            case ENUM_VALUE:
                return directivesAware instanceof GraphQLEnumValueDefinition;
            case INPUT_OBJECT:
                return directivesAware instanceof GraphQLInputObjectTypeDefinition || directivesAware instanceof GraphQLInputObjectTypeExtensionDefinition;
            case INPUT_FIELD_DEFINITION:
                return directivesAware instanceof GraphQLInputValueDefinition && !(directivesAware.getParent() instanceof GraphQLArgumentsDefinition);
            case VARIABLE_DEFINITION:
                return directivesAware instanceof GraphQLVariableDefinition;
        }
        return false;
    }


    /**
     * Gets whether the specified fragment candidate is valid to spread inside the specified required type scope
     *
     * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
     * @param fragmentCandidate      the fragment to check for being able to validly spread under the required type scope
     * @param requiredTypeScope      the type scope in which the fragment is a candidate to spread
     * @return true if the fragment candidate is valid to be spread inside the type scope
     */
    private boolean isFragmentApplicableInTypeScope(TypeDefinitionRegistry typeDefinitionRegistry,
                                                    GraphQLFragmentDefinition fragmentCandidate,
                                                    GraphQLType requiredTypeScope) {

        // unwrap non-nullable and list types
        requiredTypeScope = GraphQLSchemaUtil.getUnmodifiedType(requiredTypeScope);

        final GraphQLTypeCondition typeCondition = fragmentCandidate.getTypeCondition();
        if (typeCondition == null || typeCondition.getTypeName() == null) {
            return false;
        }

        final String fragmentTypeName = Optional.ofNullable(typeCondition.getTypeName().getName()).orElse("");
        if (fragmentTypeName.equals(GraphQLSchemaUtil.getTypeName(requiredTypeScope))) {
            // direct match, e.g. User scope, fragment on User
            return true;
        }

        // check whether compatible based on interfaces and unions
        return isCompatibleFragment(typeDefinitionRegistry, requiredTypeScope, fragmentTypeName);

    }

    /**
     * Gets whether a fragment type condition name is compatible with the required type scope
     *
     * @param typeDefinitionRegistry registry with available schema types, used to resolve union members and interface implementations
     * @param rawRequiredTypeScope   the type scope in which the fragment is a candidate to spread
     * @param fragmentTypeName       the name of the type that a candidate fragment applies to
     * @return true if the candidate type condtion name is compatible inside the required type scope
     */
    private boolean isCompatibleFragment(TypeDefinitionRegistry typeDefinitionRegistry,
                                         GraphQLType rawRequiredTypeScope,
                                         String fragmentTypeName) {

        // unwrap non-nullable and list types
        GraphQLUnmodifiedType requiredTypeScope = GraphQLSchemaUtil.getUnmodifiedType(rawRequiredTypeScope);

        if (requiredTypeScope instanceof GraphQLInterfaceType) {
            // also include fragments on types implementing the interface scope
            final TypeDefinition typeScopeDefinition = typeDefinitionRegistry.types().get(requiredTypeScope.getName());
            if (typeScopeDefinition != null) {
                final List<ObjectTypeDefinition> implementations = typeDefinitionRegistry.getImplementationsOf(
                    (InterfaceTypeDefinition) typeScopeDefinition);
                for (ObjectTypeDefinition implementation : implementations) {
                    if (implementation.getName().equals(fragmentTypeName)) {
                        return true;
                    }
                }
            }
        } else if (requiredTypeScope instanceof GraphQLObjectType) {
            // include fragments on the interfaces implemented by the object type
            for (GraphQLNamedOutputType graphQLOutputType : ((GraphQLObjectType) requiredTypeScope).getInterfaces()) {
                if (graphQLOutputType.getName().equals(fragmentTypeName)) {
                    return true;
                }
            }
        } else if (requiredTypeScope instanceof GraphQLUnionType) {
            for (GraphQLNamedOutputType graphQLOutputType : ((GraphQLUnionType) requiredTypeScope).getTypes()) {
                // check each type in the union for compatibility
                if (graphQLOutputType.getName().equals(fragmentTypeName) ||
                    isCompatibleFragment(typeDefinitionRegistry, graphQLOutputType, fragmentTypeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isIgnoredType(@NotNull TypeDefinition type) {
        return isIgnoredType(type.getName());
    }

    private static boolean isIgnoredType(@NotNull String type) {
        return GraphQLKnownTypes.isIntrospectionType(type);
    }

    @NotNull
    private CompletionResultSet updateResult(CompletionParameters params, @NotNull CompletionResultSet result) {
        CompletionResultSet completionResultSet = result;
        CompletionSorter completionSorter =
            CompletionSorter.defaultSorter(params, completionResultSet.getPrefixMatcher())
                .weighBefore("priority", new LookupElementWeigher("GraphQLWeight") {
                    @NotNull
                    @Override
                    public Comparable weigh(@NotNull LookupElement element) {
                        return new LookupElementComparator(element);
                    }
                });
        completionResultSet = completionResultSet.withRelevanceSorter(completionSorter);
        return completionResultSet;
    }

    /**
     * Moves the built-in '__' types to the bottom of the completion list by sorting them with a '|' prefix that has a higher char code value
     */
    private static class LookupElementComparator implements Comparable<LookupElementComparator> {

        private final LookupElement lookupElement;

        public LookupElementComparator(LookupElement lookupElement) {
            this.lookupElement = lookupElement;
        }

        @Override
        public int compareTo(LookupElementComparator other) {
            return getSortText(lookupElement).compareTo(getSortText(other.lookupElement));
        }

        private String getSortText(LookupElement element) {
            if (element.getLookupString().startsWith("__") || element.getLookupString().startsWith("...")) {
                return "|" + element.getLookupString();
            }
            return element.getLookupString();
        }
    }

}
