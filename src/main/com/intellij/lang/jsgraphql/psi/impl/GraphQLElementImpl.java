/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLElementImpl extends ASTWrapperPsiElement implements GraphQLElement {
  public GraphQLElementImpl(@NotNull ASTNode node) {
    super(node);
  }
}
