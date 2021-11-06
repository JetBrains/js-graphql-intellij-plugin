// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLInputObjectTypeExtensionDefinition extends GraphQLTypeExtension, GraphQLDirectivesAware, GraphQLNamedTypeExtension {

  @Nullable
  GraphQLInputObjectValueDefinitions getInputObjectValueDefinitions();

  @Nullable
  GraphQLTypeName getTypeName();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
