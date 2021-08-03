/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.icons;

import com.intellij.ide.IconProvider;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GraphQLIconProvider extends IconProvider {
    @Nullable
    @Override
    public Icon getIcon(@NotNull PsiElement element, int flags) {

        if (element instanceof GraphQLElement) {

            if (element instanceof GraphQLSelectionSetOperationDefinition) {
                return GraphQLIcons.Schema.Query;
            }

            if (element instanceof GraphQLInlineFragment) {
                return GraphQLIcons.Schema.Fragment;
            }

            if (element instanceof GraphQLTypedOperationDefinition) {
                return getOperationIcon((GraphQLTypedOperationDefinition) element);
            }

            final PsiElement parent = element.getParent();

            if (parent instanceof GraphQLTypedOperationDefinition) {
                return getOperationIcon((GraphQLTypedOperationDefinition) parent);
            }

            if (parent instanceof GraphQLEnumValue) {
                return GraphQLIcons.Schema.Enum;
            }

            if (parent instanceof GraphQLTypeNameDefinition) {
                final GraphQLTypeDefinition typeDefinition = PsiTreeUtil.getParentOfType(element, GraphQLTypeDefinition.class);
                if (typeDefinition instanceof GraphQLObjectTypeDefinition || typeDefinition instanceof GraphQLUnionTypeDefinition) {
                    return GraphQLIcons.Schema.Type;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeDefinition || typeDefinition instanceof GraphQLInputObjectTypeDefinition) {
                    return GraphQLIcons.Schema.Interface;
                }
                if (typeDefinition instanceof GraphQLScalarTypeDefinition) {
                    return GraphQLIcons.Schema.Scalar;
                }
                if (typeDefinition instanceof GraphQLEnumTypeDefinition) {
                    return GraphQLIcons.Schema.Enum;
                }
            }

            if (parent instanceof GraphQLTypeName) {

                final GraphQLTypeExtension typeDefinition = PsiTreeUtil.getParentOfType(element, GraphQLTypeExtension.class);
                if (typeDefinition instanceof GraphQLObjectTypeExtensionDefinition || typeDefinition instanceof GraphQLUnionTypeExtensionDefinition) {
                    return GraphQLIcons.Schema.Type;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeExtensionDefinition || typeDefinition instanceof GraphQLInputObjectTypeExtensionDefinition) {
                    return GraphQLIcons.Schema.Interface;
                }
                if (typeDefinition instanceof GraphQLScalarTypeExtensionDefinition) {
                    return GraphQLIcons.Schema.Scalar;
                }
                if (typeDefinition instanceof GraphQLEnumTypeExtensionDefinition) {
                    return GraphQLIcons.Schema.Enum;
                }

                if (PsiTreeUtil.getParentOfType(element, GraphQLUnionMembers.class) != null) {
                    return GraphQLIcons.Schema.Type;
                }

                if (PsiTreeUtil.getParentOfType(element, GraphQLEnumValueDefinition.class) != null) {
                    return GraphQLIcons.Schema.Enum;
                }

            }

            if (parent instanceof GraphQLFragmentDefinition || parent instanceof GraphQLInlineFragment || parent instanceof GraphQLFragmentSpread) {
                return GraphQLIcons.Schema.Fragment;
            }

            if (PsiTreeUtil.findFirstParent(element, false, p -> p instanceof GraphQLArgument || p instanceof GraphQLInputValueDefinition) != null) {
                return GraphQLIcons.Schema.Attribute;
            }
            if (PsiTreeUtil.findFirstParent(element, false, p -> p instanceof GraphQLField || p instanceof GraphQLFieldDefinition || p instanceof GraphQLObjectField) != null) {
                return GraphQLIcons.Schema.Field;
            }

            // fallback to just showing the GraphQL logo
            return GraphQLIcons.Logos.GraphQL;

        }

        return null;
    }

    private Icon getOperationIcon(GraphQLTypedOperationDefinition typedOperationDefinition) {
        final GraphQLOperationType operationType = typedOperationDefinition.getOperationType();
        switch (operationType.getText()) {
            case "query":
                return GraphQLIcons.Schema.Query;
            case "mutation":
                return GraphQLIcons.Schema.Mutation;
            case "subscription":
                return GraphQLIcons.Schema.Subscription;
            default:
                return GraphQLIcons.Logos.GraphQL;
        }
    }
}
