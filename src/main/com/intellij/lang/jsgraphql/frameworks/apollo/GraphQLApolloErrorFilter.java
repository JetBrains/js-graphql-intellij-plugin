/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.frameworks.apollo;

import com.intellij.lang.jsgraphql.ide.validation.GraphQLErrorFilter;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.psi.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.psi.GraphQLField;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;
import com.intellij.lang.jsgraphql.types.validation.ValidationErrorType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Apollo client 2.5 doesn't require sub selections for client fields
 */
public final class GraphQLApolloErrorFilter implements GraphQLErrorFilter {

  @Override
  public boolean isInspectionSuppressed(@NotNull Project project,
                                        @NotNull String toolId,
                                        @NotNull PsiElement element) {
    if (!toolId.equals(GraphQLUnresolvedReferenceInspection.SHORT_NAME)) return false;

    PsiElement parent = element.getParent();
    if (!(parent instanceof GraphQLField)) {
      return false;
    }

    return hasClientDirective(parent);
  }

  @Override
  public boolean isGraphQLErrorSuppressed(@NotNull Project project,
                                          @NotNull GraphQLError error,
                                          @Nullable PsiElement element) {
    if (!(error instanceof ValidationError)) {
      return false;
    }

    ValidationErrorType errorType = ((ValidationError)error).getValidationErrorType();
    if (errorType == ValidationErrorType.SubSelectionRequired) {
      return hasClientDirective(element);
    }
    return false;
  }

  private boolean hasClientDirective(@Nullable PsiElement element) {
    return hasClientDirective(ObjectUtils.tryCast(element, GraphQLField.class)) ||
           hasClientDirective(PsiTreeUtil.getParentOfType(element, GraphQLDirectivesAware.class));
  }

  private boolean hasClientDirective(@Nullable GraphQLDirectivesAware directivesAware) {
    if (directivesAware != null) {
      for (GraphQLDirective directive : directivesAware.getDirectives()) {
        if (GraphQLApolloKnownTypes.CLIENT_DIRECTIVE.equals(directive.getName())) {
          return true;
        }
      }
    }
    return false;
  }
}
