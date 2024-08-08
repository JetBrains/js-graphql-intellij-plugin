// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLTypedOperationDefinition extends GraphQLOperationDefinition, GraphQLDirectivesAware, GraphQLTypeScopeProvider {

  @NotNull
  GraphQLOperationType getOperationType();

  @Nullable
  GraphQLSelectionSet getSelectionSet();

  @Nullable
  GraphQLVariableDefinitions getVariableDefinitions();

  @NotNull
  List<GraphQLDirective> getDirectives();

  @Nullable
  GraphQLIdentifier getNameIdentifier();

}
