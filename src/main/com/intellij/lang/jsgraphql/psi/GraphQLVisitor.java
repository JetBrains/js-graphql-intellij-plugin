package com.intellij.lang.jsgraphql.psi;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class GraphQLVisitor extends GraphQLVisitorBase {

  public void visitGraphQLFile(@NotNull GraphQLFile file) {
    visitFile(file);
  }

  @Override
  public void visitElement(@NotNull GraphQLElement o) {
    visitElement(((PsiElement)o));
  }
}
