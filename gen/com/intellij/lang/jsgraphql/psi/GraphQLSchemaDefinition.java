// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface GraphQLSchemaDefinition extends GraphQLTypeSystemDefinition, GraphQLDirectivesAware, GraphQLDescriptionAware {

  @Nullable
  GraphQLDescription getDescription();

  @Nullable
  GraphQLOperationTypeDefinitions getOperationTypeDefinitions();

  @NotNull
  List<GraphQLDirective> getDirectives();

}
