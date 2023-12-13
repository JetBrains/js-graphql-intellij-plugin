package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.codeInsight.daemon.ChangeLocalityDetector;
import com.intellij.codeInspection.SuppressionUtilCore;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GraphQLChangeLocalityDetector implements ChangeLocalityDetector {
  @Override
  public @Nullable PsiElement getChangeHighlightingDirtyScopeFor(@NotNull PsiElement changedElement) {
    if (!changedElement.getLanguage().is(GraphQLLanguage.INSTANCE)) {
      return null;
    }

    // force the annotators to revalidate the entire file, otherwise they will only run on the modified PsiComment
    if (changedElement instanceof PsiComment && changedElement.getText().contains(SuppressionUtilCore.SUPPRESS_INSPECTIONS_TAG_NAME)) {
      return changedElement.getContainingFile();
    }
    return null;
  }
}
