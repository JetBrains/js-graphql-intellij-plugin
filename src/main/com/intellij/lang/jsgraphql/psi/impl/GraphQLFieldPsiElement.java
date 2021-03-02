/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.GraphQLConstants;
import com.intellij.lang.jsgraphql.psi.GraphQLField;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.utils.GraphQLUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLFieldPsiElement extends GraphQLNamedElementImpl implements GraphQLField, GraphQLTypeScopeProvider {
    public GraphQLFieldPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public GraphQLType getTypeScope() {
        final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getTolerantSchema(this);
        final String fieldName = this.getName();
        if (schema != null && fieldName != null) {
            // the type scope for a field is the output type of the field, given the name of the field and its parent
            final GraphQLTypeScopeProvider parentTypeScopeProvider = PsiTreeUtil.getParentOfType(this, GraphQLTypeScopeProvider.class);
            if (parentTypeScopeProvider != null) {
                GraphQLType parentType = parentTypeScopeProvider.getTypeScope();
                if (parentType != null) {
                    // found a parent operation, field, or fragment
                    parentType = GraphQLUtil.getUnmodifiedType(parentType); // unwrap list, non-null since we want a specific field
                    if (parentType instanceof GraphQLFieldsContainer) {
                        final com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition fieldDefinition = ((GraphQLFieldsContainer) parentType).getFieldDefinition(fieldName);
                        if (fieldDefinition != null) {
                            return fieldDefinition.getType();
                        } else if (fieldName.equals(GraphQLConstants.__TYPE)) {
                            return Introspection.__Type;
                        } else if (fieldName.equals(GraphQLConstants.__SCHEMA)) {
                            return Introspection.__Schema;
                        }
                    }
                }
            }
        }
        return null;
    }
}
