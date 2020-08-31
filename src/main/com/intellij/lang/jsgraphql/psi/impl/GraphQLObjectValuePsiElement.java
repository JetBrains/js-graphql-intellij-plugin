/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import graphql.schema.GraphQLInputFieldsContainer;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLObjectValuePsiElement extends GraphQLValueImpl implements GraphQLObjectValue, GraphQLTypeScopeProvider {
    public GraphQLObjectValuePsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public GraphQLType getTypeScope() {
        final PsiElement parent = getParent();
        if (parent instanceof GraphQLArgument && parent instanceof GraphQLTypeScopeProvider) {
            // this object value is an argument value, so the type scope is defined by the argument type
            return ((GraphQLTypeScopeProvider) parent).getTypeScope();
        }
        if (parent instanceof GraphQLArrayValue && parent.getParent() instanceof GraphQLTypeScopeProvider) {
            // this object value is an argument value inside an array, so the type scope is defined by the argument type
            GraphQLType typeScope = ((GraphQLTypeScopeProvider) parent.getParent()).getTypeScope();
            if (typeScope != null) {
                // unwrap non-null and array type since this object is an element in the array list type
                typeScope = GraphQLUtil.getUnmodifiedType(typeScope);
            }
            return typeScope;
        }
        if (parent instanceof GraphQLDefaultValue) {
            // this object is the default value
            final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(parent, GraphQLInputValueDefinitionImpl.class);
            if (typeScopeProvider != null) {
                return typeScopeProvider.getTypeScope();
            }
        }
        final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getTolerantSchema(this);
        if (schema != null) {
            // the type scope for an object value is a parent object value or the argument it's a value for
            final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(this, GraphQLObjectValueImpl.class);
            final GraphQLObjectField objectField = PsiTreeUtil.getParentOfType(this, GraphQLObjectField.class);
            if (typeScopeProvider != null && objectField != null) {
                GraphQLType typeScope = typeScopeProvider.getTypeScope();
                if (typeScope != null) {
                    typeScope = GraphQLUtil.getUnmodifiedType(typeScope); // unwrap list, non-null since we want a specific field
                    if (typeScope instanceof GraphQLInputFieldsContainer) {
                        final GraphQLInputObjectField inputObjectField = ((GraphQLInputFieldsContainer) typeScope).getFieldDefinition(objectField.getName());
                        if (inputObjectField != null) {
                            return inputObjectField.getType();
                        }
                    }
                }
            }
        }

        return null;
    }
}
