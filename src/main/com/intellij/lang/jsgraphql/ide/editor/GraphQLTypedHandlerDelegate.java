package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.lang.jsgraphql.psi.GraphQLArguments;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectValue;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public final class GraphQLTypedHandlerDelegate extends TypedHandlerDelegate {
  @Override
  public @NotNull Result checkAutoPopup(char charTyped, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (!(file instanceof GraphQLFile)) {
      return super.checkAutoPopup(charTyped, project, editor, file);
    }

    AutoPopupController autoPopupController = AutoPopupController.getInstance(project);
    if (charTyped == '@' || charTyped == '$' || charTyped == '[' || charTyped == '(') {
      autoPopupController.scheduleAutoPopup(editor);
      return Result.STOP;
    }

    if (charTyped == '{') {
      PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
      PsiElement parent = element != null ? element.getParent() : null;
      if (parent instanceof GraphQLArguments || parent instanceof GraphQLObjectValue) {
        autoPopupController.scheduleAutoPopup(editor);
        return Result.STOP;
      }
    }

    return super.checkAutoPopup(charTyped, project, editor, file);
  }
}
