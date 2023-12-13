/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.frameworks.relay;

import com.intellij.lang.jsgraphql.ide.validation.GraphQLErrorFilter;
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection;
import com.intellij.lang.jsgraphql.psi.GraphQLArguments;
import com.intellij.lang.jsgraphql.psi.GraphQLDirective;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Filters out errors from non-spec Relay Modern argument directives that rely on dynamically named arguments
 * that can't be expressed using SDL as of the June 2018 spec.
 */
public final class GraphQLRelayErrorFilter implements GraphQLErrorFilter {

  @Override
  public boolean isInspectionSuppressed(@NotNull Project project,
                                        @NotNull String toolId,
                                        @NotNull PsiElement element) {
    if (!toolId.equals(GraphQLUnresolvedReferenceInspection.SHORT_NAME)) return false;

    if (!GraphQLLibraryTypes.RELAY.isEnabled(project)) {
      return false;
    }

    final GraphQLArguments graphQLArguments = PsiTreeUtil.getParentOfType(element, GraphQLArguments.class);
    if (graphQLArguments != null) {
      final GraphQLDirective directive = PsiTreeUtil.getParentOfType(element, GraphQLDirective.class);
      if (directive != null) {
        final String directiveName = directive.getName();
        // ignore errors inside on the dynamically named arguments to @argumentDefinitions and @arguments
        // since the SDL can express this dynamic aspect
        return GraphQLRelayKnownTypes.ARGUMENT_DEFINITIONS_DIRECTIVE.equals(directiveName) ||
               GraphQLRelayKnownTypes.ARGUMENTS_DIRECTIVE.equals(directiveName);
      }
    }
    return false;
  }
}
