/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.intellij.ide.IconProvider;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.lang.jsgraphql.psi.GraphQLArgument;
import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValue;
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLField;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentSpread;
import com.intellij.lang.jsgraphql.psi.GraphQLInlineFragment;
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInterfaceTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLInterfaceTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectField;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLOperationType;
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLSelectionSetOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeExtension;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeName;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypedOperationDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLUnionMembers;
import com.intellij.lang.jsgraphql.psi.GraphQLUnionTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLUnionTypeExtensionDefinition;
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
                return JSGraphQLIcons.Schema.Query;
            }

            if (element instanceof GraphQLInlineFragment) {
                return JSGraphQLIcons.Schema.Fragment;
            }

            if (element instanceof GraphQLTypedOperationDefinition) {
                return getOperationIcon((GraphQLTypedOperationDefinition) element);
            }

            final PsiElement parent = element.getParent();

            if (parent instanceof GraphQLTypedOperationDefinition) {
                return getOperationIcon((GraphQLTypedOperationDefinition) parent);
            }

            if (parent instanceof GraphQLEnumValue) {
                return JSGraphQLIcons.Schema.Enum;
            }

            if (parent instanceof GraphQLTypeNameDefinition) {
                final GraphQLTypeDefinition typeDefinition = PsiTreeUtil.getParentOfType(element, GraphQLTypeDefinition.class);
                if (typeDefinition instanceof GraphQLObjectTypeDefinition || typeDefinition instanceof GraphQLUnionTypeDefinition) {
                    return JSGraphQLIcons.Schema.Type;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeDefinition || typeDefinition instanceof GraphQLInputObjectTypeDefinition) {
                    return JSGraphQLIcons.Schema.Interface;
                }
                if (typeDefinition instanceof GraphQLScalarTypeDefinition) {
                    return JSGraphQLIcons.Schema.Scalar;
                }
                if (typeDefinition instanceof GraphQLEnumTypeDefinition) {
                    return JSGraphQLIcons.Schema.Enum;
                }
            }

            if (parent instanceof GraphQLTypeName) {

                final GraphQLTypeExtension typeDefinition = PsiTreeUtil.getParentOfType(element, GraphQLTypeExtension.class);
                if (typeDefinition instanceof GraphQLObjectTypeExtensionDefinition || typeDefinition instanceof GraphQLUnionTypeExtensionDefinition) {
                    return JSGraphQLIcons.Schema.Type;
                }
                if (typeDefinition instanceof GraphQLInterfaceTypeExtensionDefinition || typeDefinition instanceof GraphQLInputObjectTypeExtensionDefinition) {
                    return JSGraphQLIcons.Schema.Interface;
                }
                if (typeDefinition instanceof GraphQLScalarTypeExtensionDefinition) {
                    return JSGraphQLIcons.Schema.Scalar;
                }
                if (typeDefinition instanceof GraphQLEnumTypeExtensionDefinition) {
                    return JSGraphQLIcons.Schema.Enum;
                }

                if (PsiTreeUtil.getParentOfType(element, GraphQLUnionMembers.class) != null) {
                    return JSGraphQLIcons.Schema.Type;
                }

                if (PsiTreeUtil.getParentOfType(element, GraphQLEnumValueDefinition.class) != null) {
                    return JSGraphQLIcons.Schema.Enum;
                }

            }

            if (parent instanceof GraphQLFragmentDefinition || parent instanceof GraphQLInlineFragment || parent instanceof GraphQLFragmentSpread) {
                return JSGraphQLIcons.Schema.Fragment;
            }

            if (PsiTreeUtil.findFirstParent(element, false, p -> p instanceof GraphQLArgument || p instanceof GraphQLInputValueDefinition) != null) {
                return JSGraphQLIcons.Schema.Attribute;
            }
            if (PsiTreeUtil.findFirstParent(element, false, p -> p instanceof GraphQLField || p instanceof GraphQLFieldDefinition || p instanceof GraphQLObjectField) != null) {
                return JSGraphQLIcons.Schema.Field;
            }

            // fallback to just showing the GraphQL logo
            return JSGraphQLIcons.Logos.GraphQL;

        }

        return null;
    }

    private Icon getOperationIcon(GraphQLTypedOperationDefinition typedOperationDefinition) {
        final GraphQLOperationType operationType = typedOperationDefinition.getOperationType();
        switch (operationType.getText()) {
            case "query":
                return JSGraphQLIcons.Schema.Query;
            case "mutation":
                return JSGraphQLIcons.Schema.Mutation;
            case "subscription":
                return JSGraphQLIcons.Schema.Subscription;
            default:
                return JSGraphQLIcons.Logos.GraphQL;
        }
    }
}
