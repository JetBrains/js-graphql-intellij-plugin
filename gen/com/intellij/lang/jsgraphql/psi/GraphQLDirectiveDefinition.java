// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLDirectiveDefinition extends GraphQLTypeSystemDefinition, GraphQLDescriptionAware {

  @Nullable
  GraphQLArgumentsDefinition getArgumentsDefinition();

  @Nullable
  GraphQLDescription getDescription();

  @Nullable
  GraphQLDirectiveLocations getDirectiveLocations();

  @Nullable
  GraphQLIdentifier getNameIdentifier();

  @Nullable
  PsiElement getRepeatable();

}
