// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLUnionTypeExtensionDefinition extends GraphQLTypeExtension, GraphQLDirectivesAware, GraphQLNamedTypeExtension {

  @Nullable
  GraphQLTypeName getTypeName();

  @Nullable
  GraphQLUnionMembership getUnionMembership();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
