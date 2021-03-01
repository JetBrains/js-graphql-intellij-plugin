/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectField;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLObjectFieldPsiElement extends GraphQLNamedElementImpl implements GraphQLObjectField, GraphQLTypeScopeProvider {
    public GraphQLObjectFieldPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public GraphQLType getTypeScope() {
        final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getTolerantSchema(this);
        if (schema != null && this.getName() != null) {
            // the type scope for an object field the type of the field as defined in the parent type scope
            final GraphQLTypeScopeProvider typeScopeProvider = PsiTreeUtil.getParentOfType(this, GraphQLTypeScopeProvider.class);
            if (typeScopeProvider != null) {
                GraphQLType typeScope = typeScopeProvider.getTypeScope();
                if (typeScope != null) {
                    typeScope = GraphQLUtil.getUnmodifiedType(typeScope); // unwrap list, non-null since we want a specific field
                    if (typeScope instanceof GraphQLInputFieldsContainer) {
                        final GraphQLInputObjectField inputObjectField = ((GraphQLInputFieldsContainer) typeScope).getFieldDefinition(this.getName());
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
