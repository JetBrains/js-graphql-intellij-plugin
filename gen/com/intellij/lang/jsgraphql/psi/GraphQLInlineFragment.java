// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;

public interface GraphQLInlineFragment extends GraphQLDirectivesAware {

  @Nullable
  GraphQLSelectionSet getSelectionSet();

  @Nullable
  GraphQLTypeCondition getTypeCondition();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
