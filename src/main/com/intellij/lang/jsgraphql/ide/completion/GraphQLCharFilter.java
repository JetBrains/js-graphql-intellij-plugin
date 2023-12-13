package com.intellij.lang.jsgraphql.ide.completion;

import com.intellij.codeInsight.lookup.CharFilter;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nullable;

public final class GraphQLCharFilter extends CharFilter {
  @Override
  public @Nullable Result acceptChar(char c, int prefixLength, Lookup lookup) {
    PsiFile file = lookup.getPsiFile();
    if (!(file instanceof GraphQLFile)) {
      return null;
    }

    // not to complete an object field from shown popup when a fragment spread is typed
    if (c == '.') {
      return Result.HIDE_LOOKUP;
    }

    return null;
  }
}
