/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLVariableMixin extends GraphQLElementImpl implements GraphQLElement, PsiNamedElement {
  public GraphQLVariableMixin(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement setName(@NotNull String s) throws IncorrectOperationException {
    throw new IncorrectOperationException("Not implemented yet");
  }

  @Override
  public String getName() {
    String text = getNode().getText();
    return text.startsWith("$") ? text.substring(1) : text;
  }
}
