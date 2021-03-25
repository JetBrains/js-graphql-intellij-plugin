package com.intellij.lang.jsgraphql.ide.validation.fixes;

import com.intellij.codeInspection.IntentionAndQuickFixAction;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLNavigateToRelatedDefinition extends IntentionAndQuickFixAction {
    private final @NotNull SmartPsiElementPointer<PsiElement> myElement;
    private final @Nullable String myName;

    public GraphQLNavigateToRelatedDefinition(@NotNull PsiElement element, @Nullable String name) {
        myElement = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
        myName = name;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return StringUtil.isEmpty(myName) ?
            getFamilyName() : GraphQLBundle.message("graphql.inspection.go.to.related.definition.name", myName);
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return GraphQLBundle.message("graphql.inspection.go.to.related.definition.family.name");
    }

    @Override
    public void applyFix(@NotNull Project project, PsiFile file, @Nullable Editor editor) {
        PsiElement element = myElement.getElement();
        if (element instanceof Navigatable) ((Navigatable) element).navigate(true);
    }
}
