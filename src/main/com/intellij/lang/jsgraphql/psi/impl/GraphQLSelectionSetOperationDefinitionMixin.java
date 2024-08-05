/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphQLSelectionSetOperationDefinitionMixin extends GraphQLNamedElementImpl implements GraphQLTypeScopeProvider {

  public GraphQLSelectionSetOperationDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable GraphQLIdentifier getNameIdentifier() {
    return null;
  }

  @Override
  public GraphQLType getTypeScope() {
    final GraphQLSchema schema = GraphQLSchemaProvider.getInstance(getProject()).getSchemaInfo(this).getSchema();
    // selection set operation definition is an anonymous query
    return schema.getQueryType();
  }
}
