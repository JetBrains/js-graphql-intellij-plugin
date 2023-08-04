package com.intellij.lang.jsgraphql.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import com.intellij.psi.PsiWalkingState;
import org.jetbrains.annotations.NotNull;

public class GraphQLRecursiveVisitor extends GraphQLVisitor implements PsiRecursiveVisitor {
  private final PsiWalkingState myWalkingState = new PsiWalkingState(this) {
    @Override
    public void elementFinished(@NotNull PsiElement element) {
      GraphQLRecursiveVisitor.this.elementFinished(element);
    }
  };

  protected void elementFinished(@NotNull PsiElement element) {
  }

  @Override
  public void visitElement(@NotNull PsiElement element) {
    super.visitElement(element);
    myWalkingState.elementStarted(element);
  }

  public void stopWalking() {
    myWalkingState.stopWalking();
  }
}
