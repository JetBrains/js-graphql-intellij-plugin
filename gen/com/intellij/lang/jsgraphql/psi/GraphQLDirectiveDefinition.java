// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDescriptionAware;

public interface GraphQLDirectiveDefinition extends GraphQLTypeSystemDefinition, GraphQLDescriptionAware {

  @Nullable
  GraphQLArgumentsDefinition getArgumentsDefinition();

  @Nullable
  GraphQLDirectiveLocations getDirectiveLocations();

  @Nullable
  GraphQLQuotedString getDescription();

  @Nullable
  GraphQLIdentifier getNameIdentifier();

}
