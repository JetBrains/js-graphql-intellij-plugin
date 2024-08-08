// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLField extends GraphQLDirectivesAware, GraphQLNamedElement, GraphQLTypeScopeProvider {

  @Nullable
  GraphQLAlias getAlias();

  @Nullable
  GraphQLArguments getArguments();

  @Nullable
  GraphQLSelectionSet getSelectionSet();

  @NotNull
  List<GraphQLDirective> getDirectives();

  @NotNull
  GraphQLIdentifier getNameIdentifier();

}
