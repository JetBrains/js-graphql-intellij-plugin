// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDescriptionAware;

public interface GraphQLInputValueDefinition extends GraphQLDirectivesAware, GraphQLNamedElement, GraphQLDescriptionAware {

  @Nullable
  GraphQLDefaultValue getDefaultValue();

  @Nullable
  GraphQLDescription getDescription();

  @Nullable
  GraphQLType getType();

  @NotNull
  List<GraphQLDirective> getDirectives();

  @NotNull
  GraphQLIdentifier getNameIdentifier();

}
