/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeScopeProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphQLInputValueDefinitionMixin extends GraphQLNamedElementImpl
  implements GraphQLInputValueDefinition, GraphQLTypeScopeProvider {

  public GraphQLInputValueDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable GraphQLType getTypeScope() {
    return GraphQLSchemaUtil.computeDeclaredType(this);
  }
}
