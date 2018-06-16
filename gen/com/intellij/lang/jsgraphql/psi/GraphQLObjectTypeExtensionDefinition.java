// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;

public interface GraphQLObjectTypeExtensionDefinition extends GraphQLTypeExtension, GraphQLDirectivesAware {

  @Nullable
  GraphQLFieldsDefinition getFieldsDefinition();

  @Nullable
  GraphQLImplementsInterfaces getImplementsInterfaces();

  @Nullable
  GraphQLTypeName getTypeName();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
