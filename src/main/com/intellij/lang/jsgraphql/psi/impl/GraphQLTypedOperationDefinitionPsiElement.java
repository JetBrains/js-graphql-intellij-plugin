/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.lang.jsgraphql.psi.GraphQLTypedOperationDefinition;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionRegistryServiceImpl;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeScopeProvider;
import com.intellij.psi.tree.IElementType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLTypedOperationDefinitionPsiElement extends GraphQLNamedElementImpl implements GraphQLTypedOperationDefinition, GraphQLTypeScopeProvider {
    public GraphQLTypedOperationDefinitionPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public GraphQLType getTypeScope() {
        final GraphQLSchema schema = GraphQLTypeDefinitionRegistryServiceImpl.getService(getProject()).getSchema(this);
        if (schema != null) {
            final IElementType operationType = getOperationType().getNode().getFirstChildNode().getElementType();
            if (operationType == GraphQLElementTypes.QUERY_KEYWORD) {
                return schema.getQueryType();
            } else if (operationType == GraphQLElementTypes.MUTATION_KEYWORD) {
                return schema.getMutationType();
            } else if (operationType == GraphQLElementTypes.SUBSCRIPTION_KEYWORD) {
                return schema.getSubscriptionType();
            }
        }
        return null;
    }
}
