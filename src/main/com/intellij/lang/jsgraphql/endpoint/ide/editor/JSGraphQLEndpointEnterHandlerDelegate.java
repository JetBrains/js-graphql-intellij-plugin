/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.ide.DataManager;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.lang.jsgraphql.endpoint.doc.psi.JSGraphQLEndpointDocFile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.DocumentUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Adds  '# ' on enter in comments
 */
public class JSGraphQLEndpointEnterHandlerDelegate extends EnterHandlerDelegateAdapter {

	@Override
	public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor, @NotNull Ref<Integer> caretOffsetRef, @NotNull Ref<Integer> caretAdvance, @NotNull DataContext dataContext, EditorActionHandler originalHandler) {

		if (file instanceof JSGraphQLEndpointDocFile) {

			int caretPos = caretOffsetRef.get();
			final TextRange lineTextRange = DocumentUtil.getLineTextRange(editor.getDocument(), editor.offsetToLogicalPosition(caretPos).line);
			final TextRange lineBeforeCaret = TextRange.create(lineTextRange.getStartOffset(), caretPos);
			final TextRange lineAfterCaret = TextRange.create(caretPos, lineTextRange.getEndOffset());

			if (!lineBeforeCaret.isEmpty()) {
				final String lineBeforeCaretText = editor.getDocument().getText(lineBeforeCaret);
				if (lineBeforeCaretText.contains("#")) {
					EditorModificationUtil.insertStringAtCaret(editor, "# ");
					if (lineAfterCaret.isEmpty()) {
						caretAdvance.set(2);
					} else {
						// if there's text after the caret, the injected doc editor will be broken into two editors
						// this means that we get an assertion error for isValid in case we try to move the caret
						// instead, we schedule a re-indent plus end of line
						if (editor instanceof EditorWindow) {
							final Project project = file.getProject();
							final Editor parentEditor = ((EditorWindow) editor).getDelegate();
							final Application application = ApplicationManager.getApplication();
							application.invokeLater(() -> {
								final PsiFile parentPsiFile = PsiDocumentManager.getInstance(project).getPsiFile(parentEditor.getDocument());
								if (parentPsiFile != null) {
								    WriteCommandAction.runWriteCommandAction(project, () -> {
                                        if(!parentPsiFile.isValid()) {
                                            return;
                                        }
                                        CodeStyleManager.getInstance(project).adjustLineIndent(parentPsiFile, parentEditor.getCaretModel().getOffset());
                                        AnAction editorLineEnd = ActionManager.getInstance().getAction("EditorLineEnd");
                                        if (editorLineEnd != null) {
                                            final AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                                                "GraphQLEndpointEnterHandler",
                                                null,
                                                DataManager.getInstance().getDataContext(parentEditor.getComponent())
                                            );
                                            editorLineEnd.actionPerformed(actionEvent);
                                        }
                                    });
								}
							});
						}
					}
				}
			}
		}

		return Result.Continue;
	}

}
