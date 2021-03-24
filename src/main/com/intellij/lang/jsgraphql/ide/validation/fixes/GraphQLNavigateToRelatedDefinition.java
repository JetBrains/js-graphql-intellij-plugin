package com.intellij.lang.jsgraphql.ide.validation.fixes;

import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLNavigateToRelatedDefinition extends IntentionAndQuickFixAction {
    @NotNull
    private final SmartPsiElementPointer<PsiElement> myElement;

    public GraphQLNavigateToRelatedDefinition(@NotNull PsiElement element) {
        myElement = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return GraphQLBundle.message("graphql.inspection.go.to.related.definition");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return getName();
    }

    @Override
    public void applyFix(@NotNull Project project, PsiFile file, @Nullable Editor editor) {
        PsiElement element = myElement.getElement();
        if (element instanceof Navigatable) ((Navigatable) element).navigate(true);
    }
}
