/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.resolve;

import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLIdentifierImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Implements renaming of GraphQL identifiers
 */
public final class GraphQLIdentifierManipulator extends AbstractElementManipulator<GraphQLIdentifierImpl> {

  @Override
  public GraphQLIdentifierImpl handleContentChange(@NotNull GraphQLIdentifierImpl element, @NotNull TextRange range, String newContent)
    throws IncorrectOperationException {
    // replace the NAME leaf element inside the identifier
    final LeafElement renamedLeaf = Factory.createSingleLeafElement(GraphQLElementTypes.NAME, newContent, null, element.getManager());
    final PsiElement renamedPsiElement = SourceTreeToPsiMap.treeElementToPsi(renamedLeaf);
    if (renamedPsiElement != null) {
      element.getFirstChild().replace(renamedPsiElement);
    }
    return element;
  }
}
