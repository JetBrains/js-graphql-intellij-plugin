// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLVariableDefinition extends GraphQLDirectivesAware, GraphQLDescriptionAware, GraphQLTypeScopeProvider, GraphQLTypeOwner {

  @Nullable
  GraphQLDefaultValue getDefaultValue();

  @Nullable
  GraphQLDescription getDescription();

  @Nullable
  GraphQLType getType();

  @NotNull
  GraphQLVariable getVariable();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
