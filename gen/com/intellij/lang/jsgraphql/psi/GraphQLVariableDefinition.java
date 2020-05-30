// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;

public interface GraphQLVariableDefinition extends GraphQLDirectivesAware {

  @Nullable
  GraphQLDefaultValue getDefaultValue();

  @Nullable
  GraphQLType getType();

  @NotNull
  GraphQLVariable getVariable();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
