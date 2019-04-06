// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;

public interface GraphQLTypedOperationDefinition extends GraphQLOperationDefinition, GraphQLDirectivesAware {

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
