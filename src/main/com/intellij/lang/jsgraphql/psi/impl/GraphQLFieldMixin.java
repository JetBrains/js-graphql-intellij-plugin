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
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.introspection.Introspection;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldsContainer;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLFieldMixin extends GraphQLNamedElementImpl implements GraphQLField, GraphQLTypeScopeProvider {
    public GraphQLFieldMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public GraphQLType getTypeScope() {
        final String fieldName = this.getName();
        if (fieldName != null) {
            // the type scope for a field is the output type of the field, given the name of the field and its parent
            final GraphQLTypeScopeProvider parentTypeScopeProvider = PsiTreeUtil.getParentOfType(this, GraphQLTypeScopeProvider.class);
            if (parentTypeScopeProvider != null) {
                GraphQLType parentType = parentTypeScopeProvider.getTypeScope();
                if (parentType != null) {
                    // found a parent operation, field, or fragment
                    parentType = GraphQLSchemaUtil.getUnmodifiedType(parentType); // unwrap list, non-null since we want a specific field
                    if (parentType instanceof GraphQLFieldsContainer) {
                        final com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition fieldDefinition = ((GraphQLFieldsContainer) parentType).getFieldDefinition(fieldName);
                        if (fieldDefinition != null) {
                            return fieldDefinition.getType();
                        } else if (fieldName.equals(GraphQLConstants.Schema.__TYPE)) {
                            return Introspection.__Type;
                        } else if (fieldName.equals(GraphQLConstants.Schema.__SCHEMA)) {
                            return Introspection.__Schema;
                        }
                    }
                }
            }
        }
        return null;
    }
}
