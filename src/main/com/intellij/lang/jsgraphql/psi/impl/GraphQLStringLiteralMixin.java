package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLStringLiteral;

public abstract class GraphQLStringLiteralMixin extends GraphQLElementImpl implements GraphQLStringLiteral {

  public GraphQLStringLiteralMixin(ASTNode node) {
    super(node);
  }
}
