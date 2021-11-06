// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLInterfaceTypeExtensionDefinition extends GraphQLTypeExtension, GraphQLDirectivesAware, GraphQLNamedTypeExtension {

  @Nullable
  GraphQLFieldsDefinition getFieldsDefinition();

  @Nullable
  GraphQLImplementsInterfaces getImplementsInterfaces();

  @Nullable
  GraphQLTypeName getTypeName();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
