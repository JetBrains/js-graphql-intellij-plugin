package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLVariableDefinition;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaUtil;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GraphQLVariableDefinitionMixin extends GraphQLElementImpl implements GraphQLVariableDefinition {

  public GraphQLVariableDefinitionMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable GraphQLType getTypeScope() {
    return GraphQLSchemaUtil.computeDeclaredType(this);
  }
}
