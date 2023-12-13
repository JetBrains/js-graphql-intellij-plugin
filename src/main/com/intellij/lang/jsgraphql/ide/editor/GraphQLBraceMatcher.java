/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GraphQLBraceMatcher implements PairedBraceMatcher {
  @Override
  public BracePair[] getPairs() {
    return new BracePair[]{
      new BracePair(GraphQLElementTypes.PAREN_L, GraphQLElementTypes.PAREN_R, true),
      new BracePair(GraphQLElementTypes.BRACKET_L, GraphQLElementTypes.BRACKET_R, true),
      new BracePair(GraphQLElementTypes.BRACE_L, GraphQLElementTypes.BRACE_R, true)
    };
  }

  @Override
  public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, @Nullable IElementType contextType) {
    return true;
  }

  @Override
  public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
    return openingBraceOffset;
  }
}
