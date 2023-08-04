package com.intellij.lang.jsgraphql.ide.validation;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GraphQLErrorFilter {
  ExtensionPointName<GraphQLErrorFilter> EP_NAME = new ExtensionPointName<>("com.intellij.lang.jsgraphql.errorFilter");

  default boolean isGraphQLErrorSuppressed(@NotNull Project project, @NotNull GraphQLError error, @Nullable PsiElement element) {
    return false;
  }

  default boolean isInspectionSuppressed(@NotNull Project project,
                                         @NotNull String toolId,
                                         @NotNull PsiElement element) {
    return false;
  }
}
