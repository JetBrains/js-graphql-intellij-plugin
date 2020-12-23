package com.intellij.lang.jsgraphql.psi;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public class GraphQLRecursiveVisitor extends GraphQLVisitor implements PsiRecursiveVisitor {
    @Override
    public void visitElement(@NotNull PsiElement element) {
        ProgressManager.checkCanceled();
        element.acceptChildren(this);
    }

    @Override
    public void visitElement(@NotNull GraphQLElement o) {
        visitElement((PsiElement) o);
    }
}
