/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

/**
 * Adds quotes after the import keyword and toggles completion on available file names
 */
public class JSGraphQLEndpointImportInsertHandler implements InsertHandler<LookupElement> {

  final static InsertHandler<LookupElement> INSTANCE_WITH_AUTO_POPUP = new JSGraphQLEndpointImportInsertHandler(true);

  private final boolean myTriggerAutoPopup;

  JSGraphQLEndpointImportInsertHandler(boolean triggerAutoPopup) {
    myTriggerAutoPopup = triggerAutoPopup;
  }

  public void handleInsert(InsertionContext context, LookupElement item) {
    final Editor editor = context.getEditor();
    final Project project = editor.getProject();
    if (project != null) {
      EditorModificationUtil.insertStringAtCaret(editor, " \"\"");
      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() - 1);
      if (myTriggerAutoPopup) {
        AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
      }
    }
  }
}
