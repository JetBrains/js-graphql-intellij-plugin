/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.references;

import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectiveImpl;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLFieldImpl;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLReferencePsiElement;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeScopeProvider;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.schema.GraphQLType;
import graphql.schema.SchemaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

public class GraphQLReferenceService {

    public static GraphQLReferenceService getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLReferenceService.class);
    }

    public PsiReference resolveReference(GraphQLReferencePsiElement element) {
        if (element != null) {
            final PsiElement parent = element.getParent();
            if (parent instanceof GraphQLField) {
                return resolveFieldReference(element, (GraphQLField) parent);
            }
            if (parent instanceof GraphQLFragmentSpread) {
                return resolveFragmentDefinition(element);
            }
            if (parent instanceof GraphQLArgument) {
                return resolveArgument(element);
            }
            if (parent instanceof GraphQLObjectField) {
                return resolveObjectField(element, (GraphQLObjectField) parent);
            }
            if (parent instanceof GraphQLTypeName) {
                return resolveTypeName(element);
            }
            if (parent instanceof GraphQLEnumValue) {
                return resolveEnumValue(element);
            }
            if (parent instanceof GraphQLDirective) {
                return resolveDirective(element);
            }
            if (parent instanceof GraphQLFieldDefinition) {
                return resolveFieldDefinition(element, (GraphQLFieldDefinition) parent);
            }
        }
        return null;
    }

    private PsiReference resolveFieldDefinition(GraphQLReferencePsiElement element, GraphQLFieldDefinition fieldDefinition) {
        final String name = fieldDefinition.getName();
        if (name != null) {
            final GraphQLTypeSystemDefinition typeSystemDefinition = PsiTreeUtil.getParentOfType(element, GraphQLTypeSystemDefinition.class);
            if (typeSystemDefinition != null) {
                final GraphQLImplementsInterfaces implementsInterfaces = PsiTreeUtil.findChildOfType(typeSystemDefinition, GraphQLImplementsInterfaces.class);
                if (implementsInterfaces != null) {
                    for (GraphQLTypeName implementedType : implementsInterfaces.getTypeNameList()) {
                        final PsiReference typeReference = implementedType.getNameIdentifier().getReference();
                        if (typeReference != null) {
                            final PsiElement interfaceIdentifier = typeReference.resolve();
                            if (interfaceIdentifier != null) {
                                final GraphQLTypeSystemDefinition interfaceDefinition = PsiTreeUtil.getParentOfType(interfaceIdentifier, GraphQLTypeSystemDefinition.class);
                                final Collection<GraphQLFieldDefinition> interfaceFieldDefinitions = PsiTreeUtil.findChildrenOfType(interfaceDefinition, GraphQLFieldDefinition.class);
                                for (GraphQLFieldDefinition interfaceFieldDefinition : interfaceFieldDefinitions) {
                                    if (name.equals(interfaceFieldDefinition.getName())) {
                                        return createReference(element, interfaceFieldDefinition.getNameIdentifier());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private PsiReference resolveArgument(GraphQLReferencePsiElement element) {
        final String name = element.getName();
        if (name != null) {
            final GraphQLDirectiveImpl directive = PsiTreeUtil.getParentOfType(element, GraphQLDirectiveImpl.class);
            // directive argument
            if (directive != null) {
                final GraphQLIdentifier directiveNameIdentifier = directive.getNameIdentifier();
                if (directiveNameIdentifier != null) {
                    final PsiReference directiveReference = directiveNameIdentifier.getReference();
                    if (directiveReference != null) {
                        final PsiElement directiveIdentifier = directiveReference.resolve();
                        if (directiveIdentifier != null) {
                            final GraphQLArgumentsDefinition arguments = PsiTreeUtil.getNextSiblingOfType(directiveIdentifier, GraphQLArgumentsDefinition.class);
                            if (arguments != null) {
                                for (GraphQLInputValueDefinition inputValueDefinition : arguments.getInputValueDefinitionList()) {
                                    if (name.equals(inputValueDefinition.getName())) {
                                        return createInputValueDefinitionReference(element, inputValueDefinition);
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
            // field argument
            final GraphQLFieldImpl field = PsiTreeUtil.getParentOfType(element, GraphQLFieldImpl.class);
            if (field != null) {
                final PsiReference fieldPsiReference = field.getNameIdentifier().getReference();
                GraphQLFieldDefinition fieldDefinition = null;
                if (fieldPsiReference != null) {
                    final PsiElement resolvedPsiReference = fieldPsiReference.resolve();
                    if (resolvedPsiReference != null && resolvedPsiReference.getParent() instanceof GraphQLFieldDefinition) {
                        fieldDefinition = (GraphQLFieldDefinition) resolvedPsiReference.getParent();
                    }
                }
                if (fieldDefinition != null) {
                    final GraphQLArgumentsDefinition argumentsDefinition = fieldDefinition.getArgumentsDefinition();
                    if (argumentsDefinition != null) {
                        for (GraphQLInputValueDefinition inputValueDefinition : argumentsDefinition.getInputValueDefinitionList()) {
                            if (name.equals(inputValueDefinition.getName())) {
                                return createInputValueDefinitionReference(element, inputValueDefinition);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    PsiReference createInputValueDefinitionReference(GraphQLReferencePsiElement element, GraphQLInputValueDefinition inputValueDefinition) {
        return createReference(element, inputValueDefinition.getNameIdentifier());
    }

    PsiReference resolveFieldReference(GraphQLReferencePsiElement element, GraphQLField field) {
        final String name = element.getName();
        Ref<PsiReference> reference = new Ref<>();
        if (name != null) {
            final GraphQLPsiSearchHelper graphQLPsiSearchHelper = GraphQLPsiSearchHelper.getService(element.getProject());
            if(name.startsWith("__")) {
                // __typename or introspection fields __schema and __type which implicitly extends the query root type
                graphQLPsiSearchHelper.getBuiltInSchema().accept(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(final PsiElement schemaElement) {
                        if(schemaElement instanceof GraphQLReferencePsiElement && schemaElement.getText().equals(name)) {
                            reference.set(createReference(element, schemaElement));
                            return;
                        }
                        super.visitElement(schemaElement);
                    }
                });
            }
            final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
            if (reference.isNull() && typeScopeProvider != null) {
                GraphQLType typeScope = typeScopeProvider.getTypeScope();
                if (typeScope != null) {
                    final GraphQLType fieldType = new SchemaUtil().getUnmodifiedType(typeScope);
                    graphQLPsiSearchHelper.processElementsWithWord(element, name, psiNamedElement -> {
                        // TODO JKM Also support Endpoint references
                        if (psiNamedElement.getParent() instanceof com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition) {
                            boolean isTypeMatch = false;
                            final GraphQLTypeDefinition typeDefinition = PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLTypeDefinition.class);
                            if (typeDefinition != null) {
                                final GraphQLTypeNameDefinition typeNameDefinition = PsiTreeUtil.findChildOfType(typeDefinition, GraphQLTypeNameDefinition.class);
                                isTypeMatch = typeNameDefinition != null && fieldType.getName().equals(typeNameDefinition.getName());
                            }
                            if (!isTypeMatch) {
                                // check type extension
                                final GraphQLTypeExtension typeExtension = PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLTypeExtension.class);
                                if (typeExtension != null) {
                                    final GraphQLTypeName typeName = PsiTreeUtil.findChildOfType(typeExtension, GraphQLTypeName.class);
                                    isTypeMatch = typeName != null && fieldType.getName().equals(typeName.getName());
                                }
                            }
                            if (isTypeMatch) {
                                reference.set(createReference(element, psiNamedElement));
                                return false; // done searching
                            }
                        }
                        return true;
                    });
                }
            }
        }
        return reference.get();
    }

    @NotNull
    private PsiReferenceBase<GraphQLReferencePsiElement> createReference(GraphQLReferencePsiElement fromElement, PsiElement resolvedElement) {
        return new PsiReferenceBase<GraphQLReferencePsiElement>(fromElement, TextRange.from(0, fromElement.getTextLength())) {
            @Override
            public PsiElement resolve() {
                return resolvedElement;
            }

            @NotNull
            @Override
            public Object[] getVariants() {
                return PsiReference.EMPTY_ARRAY;
            }
        };
    }


    PsiReference resolveTypeName(GraphQLReferencePsiElement element) {
        return resolveUsingIndex(element, psiNamedElement -> psiNamedElement instanceof GraphQLIdentifier && psiNamedElement.getParent() instanceof GraphQLTypeNameDefinition);
    }


    PsiReference resolveFragmentDefinition(GraphQLReferencePsiElement element) {
        return resolveUsingIndex(element, psiNamedElement -> psiNamedElement instanceof GraphQLIdentifier && psiNamedElement.getParent() instanceof GraphQLFragmentDefinition);
    }

    private PsiReference resolveObjectField(GraphQLReferencePsiElement element, GraphQLObjectField field) {
        final String name = element.getName();
        if (name != null) {
            final GraphQLTypeScopeProvider fieldTypeScopeProvider = PsiTreeUtil.getParentOfType(field, GraphQLTypeScopeProvider.class);
            if (fieldTypeScopeProvider != null) {
                GraphQLType typeScope = fieldTypeScopeProvider.getTypeScope();
                if (typeScope != null) {
                    final String namedTypeScope = new SchemaUtil().getUnmodifiedType(typeScope).getName();
                    return resolveUsingIndex(element, psiNamedElement -> {
                        if (psiNamedElement.getParent() instanceof GraphQLInputValueDefinition) {
                            final GraphQLInputObjectTypeDefinition inputTypeDefinition = PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLInputObjectTypeDefinition.class);
                            if (inputTypeDefinition != null && inputTypeDefinition.getTypeNameDefinition() != null) {
                                if (namedTypeScope.equals(inputTypeDefinition.getTypeNameDefinition().getName())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                }
            }
        }
        return null;
    }

    private PsiReference resolveEnumValue(GraphQLReferencePsiElement element) {
        final String name = element.getName();
        if (name != null) {
            final GraphQLTypeScopeProvider enumTypeScopeProvider = PsiTreeUtil.getParentOfType(element, GraphQLTypeScopeProvider.class);
            if (enumTypeScopeProvider != null) {
                GraphQLType typeScope = enumTypeScopeProvider.getTypeScope();
                if (typeScope != null) {
                    final String namedTypeScope = new SchemaUtil().getUnmodifiedType(typeScope).getName();
                    return resolveUsingIndex(element, psiNamedElement -> {
                        if (psiNamedElement.getParent() instanceof GraphQLEnumValue) {
                            final GraphQLEnumTypeDefinition enumTypeDefinition = PsiTreeUtil.getParentOfType(psiNamedElement, GraphQLEnumTypeDefinition.class);
                            if (enumTypeDefinition != null && enumTypeDefinition.getTypeNameDefinition() != null) {
                                if (namedTypeScope.equals(enumTypeDefinition.getTypeNameDefinition().getName())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                }
            }
        }
        return null;
    }

    private PsiReference resolveDirective(GraphQLReferencePsiElement element) {
        return resolveUsingIndex(element, psiNamedElement -> psiNamedElement instanceof GraphQLIdentifier && psiNamedElement.getParent() instanceof GraphQLDirectiveDefinition);
    }


    private PsiReference resolveUsingIndex(GraphQLReferencePsiElement element, Predicate<PsiNamedElement> isMatch) {
        final String name = element.getName();
        Ref<PsiReference> reference = new Ref<>();
        if (name != null) {
            GraphQLPsiSearchHelper.getService(element.getProject()).processElementsWithWord(element, name, psiNamedElement -> {
                if (isMatch.test(psiNamedElement)) {
                    reference.set(new PsiReferenceBase<PsiNamedElement>(element, TextRange.from(0, element.getTextLength())) {
                        @Nullable
                        @Override
                        public PsiElement resolve() {
                            return psiNamedElement;
                        }

                        @NotNull
                        @Override
                        public Object[] getVariants() {
                            return PsiReference.EMPTY_ARRAY;
                        }
                    });
                    return false; // done searching
                }
                return true;
            });
        }
        return reference.get();
    }
}
