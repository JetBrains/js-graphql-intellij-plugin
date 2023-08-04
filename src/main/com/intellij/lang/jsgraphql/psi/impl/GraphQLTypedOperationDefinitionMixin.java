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
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLTypedOperationDefinition;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLTypedOperationDefinitionMixin extends GraphQLNamedElementImpl
  implements GraphQLTypedOperationDefinition, GraphQLTypeScopeProvider {
  public GraphQLTypedOperationDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public GraphQLType getTypeScope() {
    final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getSchemaInfo(this).getSchema();
    final IElementType operationType = getOperationType().getNode().getFirstChildNode().getElementType();
    if (operationType == GraphQLElementTypes.QUERY_KEYWORD) {
      return schema.getQueryType();
    }
    else if (operationType == GraphQLElementTypes.MUTATION_KEYWORD) {
      return schema.getMutationType();
    }
    else if (operationType == GraphQLElementTypes.SUBSCRIPTION_KEYWORD) {
      return schema.getSubscriptionType();
    }
    return null;
  }
}
