/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeName;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeDefinitionRegistryServiceImpl;
import com.intellij.lang.jsgraphql.schema.GraphQLTypeScopeProvider;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLFragmentDefinitionPsiElement extends GraphQLNamedElementImpl implements GraphQLFragmentDefinition, GraphQLTypeScopeProvider {
    public GraphQLFragmentDefinitionPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public GraphQLType getTypeScope() {
        final GraphQLSchema schema = GraphQLTypeDefinitionRegistryServiceImpl.getService(getProject()).getSchema(this);
        if (schema != null) {
            if(getTypeCondition() != null) {
                final GraphQLTypeName typeName = getTypeCondition().getTypeName();
                if(typeName != null) {
                    return schema.getType(typeName.getText());
                }
            }
        }
        return null;
    }
}
