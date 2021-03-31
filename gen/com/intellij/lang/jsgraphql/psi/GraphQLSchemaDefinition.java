// This is a generated file. Not intended for manual editing.
package com.intellij.lang.jsgraphql.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDirectivesAware;
import com.intellij.lang.jsgraphql.psi.impl.GraphQLDescriptionAware;

public interface GraphQLSchemaDefinition extends GraphQLTypeSystemDefinition, GraphQLDirectivesAware, GraphQLDescriptionAware {

  @Nullable
  GraphQLOperationTypeDefinitions getOperationTypeDefinitions();

  @NotNull
  List<GraphQLDirective> getDirectives();

  @Nullable
  GraphQLQuotedString getDescription();

}
