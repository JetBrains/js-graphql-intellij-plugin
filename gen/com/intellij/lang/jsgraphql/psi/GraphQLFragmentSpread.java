// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLFragmentSpread extends GraphQLDirectivesAware, GraphQLNamedElement {

  @NotNull
  List<GraphQLDirective> getDirectives();

  @NotNull
  GraphQLIdentifier getNameIdentifier();

}
