/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.structureView;

import com.google.common.collect.Lists;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLArguments;
import com.intellij.lang.jsgraphql.psi.GraphQLArgumentsDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinitions;
import com.intellij.lang.jsgraphql.psi.GraphQLField;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldsDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentSelection;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentSpread;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLInlineFragment;
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectValueDefinitions;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.lang.jsgraphql.psi.GraphQLOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLSelection;
import com.intellij.lang.jsgraphql.psi.GraphQLSelectionSet;
import com.intellij.lang.jsgraphql.psi.GraphQLSelectionSetOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeCondition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeName;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeSystemDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypedOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLUnionMembers;
import com.intellij.lang.jsgraphql.psi.GraphQLUnionMembership;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * A node in the GraphQL structure tree view
 */
public class GraphQLStructureViewTreeElement extends PsiTreeElementBase<PsiElement> {

    final PsiElement childrenBase;
    private final PsiElement element;

    public GraphQLStructureViewTreeElement(PsiElement childrenBase, PsiElement psiElement) {
        super(psiElement);
        this.element = psiElement;
        this.childrenBase = childrenBase;
    }

    @NotNull
    @Override
    public Collection<StructureViewTreeElement> getChildrenBase() {
        final List<StructureViewTreeElement> children = Lists.newArrayList();

        // See GraphQLParser.bnf for structure reference

        if (childrenBase instanceof GraphQLFile) {

            addFileChildren(children);

        } else if (childrenBase instanceof GraphQLSelectionSet) {

            addSelectionSetChildren(children);

        } else if (childrenBase instanceof GraphQLSelectionSetOperationDefinition) {

            addSelectionSetChildren(children);

        } else if (childrenBase instanceof GraphQLField) {

            addFieldChildren(children);

        } else if (childrenBase instanceof GraphQLFieldsDefinition) {

            for (GraphQLFieldDefinition fieldDefinition : ((GraphQLFieldsDefinition) childrenBase).getFieldDefinitionList()) {
                children.add(new GraphQLStructureViewTreeElement(fieldDefinition, fieldDefinition.getNameIdentifier()));
            }

        } else if (childrenBase instanceof GraphQLEnumValueDefinitions) {

            for (GraphQLEnumValueDefinition enumValueDefinition : ((GraphQLEnumValueDefinitions) childrenBase).getEnumValueDefinitionList()) {
                final GraphQLIdentifier nameIdentifier = enumValueDefinition.getEnumValue().getNameIdentifier();
                children.add(new GraphQLStructureViewTreeElement(nameIdentifier, nameIdentifier));
            }

        } else if (childrenBase instanceof GraphQLInputObjectValueDefinitions) {

            for (GraphQLInputValueDefinition valueDefinition : ((GraphQLInputObjectValueDefinitions) childrenBase).getInputValueDefinitionList()) {
                final GraphQLIdentifier nameIdentifier = valueDefinition.getNameIdentifier();
                children.add(new GraphQLStructureViewTreeElement(nameIdentifier, nameIdentifier));
            }

        } else if (childrenBase instanceof GraphQLUnionMembership) {

            final GraphQLUnionMembers unionMembers = ((GraphQLUnionMembership) childrenBase).getUnionMembers();
            if (unionMembers != null) {
                for (GraphQLTypeName unionTypeName : unionMembers.getTypeNameList()) {
                    final GraphQLIdentifier nameIdentifier = unionTypeName.getNameIdentifier();
                    children.add(new GraphQLStructureViewTreeElement(nameIdentifier, nameIdentifier));
                }
            }

        } else if (childrenBase instanceof GraphQLFieldDefinition) {

            final GraphQLArgumentsDefinition argumentsDefinition = ((GraphQLFieldDefinition) childrenBase).getArgumentsDefinition();
            if (argumentsDefinition != null) {
                for (GraphQLInputValueDefinition valueDefinition : argumentsDefinition.getInputValueDefinitionList()) {
                    final GraphQLIdentifier nameIdentifier = valueDefinition.getNameIdentifier();
                    children.add(new GraphQLStructureViewTreeElement(nameIdentifier, nameIdentifier));
                }
            }

        }

        return children;
    }

    private void addFieldChildren(List<StructureViewTreeElement> children) {
        final GraphQLField field = (GraphQLField) this.childrenBase;
        final GraphQLArguments arguments = field.getArguments();
        if (arguments != null) {
            for (GraphQLArgument argument : arguments.getArgumentList()) {
                children.add(new GraphQLStructureViewTreeElement(argument, argument.getNameIdentifier()));
            }
        }
        if (field.getSelectionSet() != null) {
            addSelectionSetChildren(children);
        }
    }

    private void addSelectionSetChildren(List<StructureViewTreeElement> children) {
        GraphQLSelectionSet selectionSet;
        if (childrenBase instanceof GraphQLSelectionSet) {
            selectionSet = (GraphQLSelectionSet) childrenBase;
        } else if (childrenBase instanceof GraphQLSelectionSetOperationDefinition) {
            selectionSet = ((GraphQLSelectionSetOperationDefinition) childrenBase).getSelectionSet();
        } else if (childrenBase instanceof GraphQLField) {
            selectionSet = ((GraphQLField) childrenBase).getSelectionSet();
        } else {
            return;
        }
        for (GraphQLSelection selection : selectionSet.getSelectionList()) {
            final GraphQLField field = selection.getField();
            if (field != null) {
                children.add(new GraphQLStructureViewTreeElement(field, field.getNameIdentifier()));
            } else {
                final GraphQLFragmentSelection fragmentSelection = selection.getFragmentSelection();
                if (fragmentSelection != null) {
                    GraphQLFragmentSpread fragmentSpread = fragmentSelection.getFragmentSpread();
                    if (fragmentSpread != null) {
                        children.add(new GraphQLStructureViewTreeElement(fragmentSpread, fragmentSpread.getNameIdentifier()));
                    } else {
                        GraphQLInlineFragment inlineFragment = fragmentSelection.getInlineFragment();
                        if (inlineFragment != null && inlineFragment.getSelectionSet() != null) {
                            if (inlineFragment.getTypeCondition() != null && inlineFragment.getTypeCondition().getTypeName() != null) {
                                children.add(new GraphQLStructureViewTreeElement(inlineFragment.getSelectionSet(), inlineFragment));
                            }
                        }
                    }
                }
            }
        }
    }

    private void addFileChildren(List<StructureViewTreeElement> children) {

        for (PsiElement child : childrenBase.getChildren()) {

            PsiElement nodeChildrenBase = child;
            PsiElement nodeElement = child;

            if (child instanceof GraphQLOperationDefinition) {

                GraphQLIdentifier nameIdentifier = ((GraphQLOperationDefinition) child).getNameIdentifier();
                if (nameIdentifier != null) {
                    nodeElement = nameIdentifier;
                }
                if (child instanceof GraphQLTypedOperationDefinition) {
                    nodeChildrenBase = ((GraphQLTypedOperationDefinition) child).getSelectionSet();
                }
                children.add(new GraphQLStructureViewTreeElement(nodeChildrenBase, nodeElement));

            } else if (child instanceof GraphQLFragmentDefinition) {

                GraphQLIdentifier nameIdentifier = ((GraphQLFragmentDefinition) child).getNameIdentifier();
                if (nameIdentifier != null) {
                    nodeElement = nameIdentifier;
                }
                nodeChildrenBase = ((GraphQLFragmentDefinition) child).getSelectionSet();

                children.add(new GraphQLStructureViewTreeElement(nodeChildrenBase, nodeElement));

            } else if (child instanceof GraphQLTypeSystemDefinition) {

                // the name of the node is type name def for schema definitions, and type nae for schema type extensions
                GraphQLNamedElement schemaNodeElement = PsiTreeUtil.findChildOfAnyType(child, GraphQLTypeNameDefinition.class, GraphQLTypeName.class);

                // children of the type definitions/extensions are found in the follow element types
                PsiElement schemaNodeChildrenBase = PsiTreeUtil.findChildOfAnyType(
                        child,
                        GraphQLFieldsDefinition.class,
                        GraphQLEnumValueDefinitions.class,
                        GraphQLInputObjectValueDefinitions.class,
                        GraphQLUnionMembership.class
                );

                if (schemaNodeElement != null && schemaNodeChildrenBase != null) {
                    final PsiElement nodeIdentifier = schemaNodeElement.getNameIdentifier() != null ? schemaNodeElement.getNameIdentifier() : schemaNodeElement;
                    children.add(new GraphQLStructureViewTreeElement(schemaNodeChildrenBase, nodeIdentifier));
                }

            }
        }
    }

    @Nullable
    @Override
    public String getPresentableText() {

        if (element instanceof GraphQLSelectionSetOperationDefinition) {
            return "anonymous query"; // "{}" selection as root, which corresponds to anonymous query
        }

        if (element instanceof GraphQLInlineFragment) {
            String text = "... on";
            GraphQLTypeCondition typeCondition = ((GraphQLInlineFragment) element).getTypeCondition();
            if (typeCondition != null && typeCondition.getTypeName() != null) {
                text += " " + typeCondition.getTypeName().getName();
            }
            return text;
        }

        if (element instanceof GraphQLNamedElement) {
            String name = ((GraphQLNamedElement) element).getName();
            if (name == null && element instanceof GraphQLTypedOperationDefinition) {
                return "anonymous query"; // "query(args) {}"
            }
            return name;
        }

        if (element instanceof PsiNameIdentifierOwner) {
            final PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
            if (nameIdentifier != null) {
                return nameIdentifier.getText();
            }
        }

        return element.getText();
    }
}
