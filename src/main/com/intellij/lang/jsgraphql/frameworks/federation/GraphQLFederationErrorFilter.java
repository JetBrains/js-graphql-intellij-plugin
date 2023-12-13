package com.intellij.lang.jsgraphql.frameworks.federation;

import com.intellij.lang.jsgraphql.ide.validation.GraphQLErrorFilter;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.language.NamedNode;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.EmptyUnionTypeError;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class GraphQLFederationErrorFilter implements GraphQLErrorFilter {

  @Override
  public boolean isGraphQLErrorSuppressed(@NotNull Project project,
                                          @NotNull GraphQLError error,
                                          @Nullable PsiElement element) {
    if (!GraphQLLibraryTypes.FEDERATION.isEnabled(project)) {
      return false;
    }

    if (error instanceof EmptyUnionTypeError && error.getNode() instanceof NamedNode) {
      return Objects.equals(((NamedNode<?>)error.getNode()).getName(), GraphQLFederationKnownTypes.ENTITY);
    }

    return false;
  }
}
