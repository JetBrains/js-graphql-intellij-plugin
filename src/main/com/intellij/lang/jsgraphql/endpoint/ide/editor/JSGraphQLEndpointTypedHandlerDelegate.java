package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.jsgraphql.endpoint.psi.JSGraphQLEndpointFile;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLEndpointTypedHandlerDelegate extends TypedHandlerDelegate {
    @Override
    public @NotNull Result checkAutoPopup(char charTyped, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (!(file instanceof JSGraphQLEndpointFile)) {
            return super.checkAutoPopup(charTyped, project, editor, file);
        }

        AutoPopupController autoPopupController = AutoPopupController.getInstance(project);
        if (charTyped == '@') {
            autoPopupController.autoPopupMemberLookup(editor, null);
            return Result.STOP;
        }

        return super.checkAutoPopup(charTyped, project, editor, file);
    }
}
