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
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public abstract class GraphQLNamedElementImpl extends GraphQLElementImpl implements GraphQLNamedElement {
  public GraphQLNamedElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getName() {
    PsiElement identifier = getNameIdentifier();
    if (identifier == null) return null;

    ASTNode identifierNode = identifier.getNode();
    if (identifierNode == null) return null;

    return identifierNode.getText();
  }

  @Override
  public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
    final GraphQLIdentifier nameIdentifier = getNameIdentifier();
    if (nameIdentifier != null) {
      final LeafElement renamedLeaf = Factory.createSingleLeafElement(GraphQLElementTypes.NAME, newName, null, nameIdentifier.getManager());
      final PsiElement renamedPsiElement = SourceTreeToPsiMap.treeElementToPsi(renamedLeaf);
      if (renamedPsiElement != null) {
        nameIdentifier.getFirstChild().replace(renamedPsiElement);
      }
    }
    return this;
  }
}
