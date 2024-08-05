/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDocumentManager;

public class AddColonSpaceInsertHandler implements InsertHandler<LookupElement> {

  public static final InsertHandler<LookupElement> INSTANCE_WITH_AUTO_POPUP = new AddColonSpaceInsertHandler(true);

  private final String myIgnoreOnChars;
  private final boolean myTriggerAutoPopup;

  public AddColonSpaceInsertHandler(boolean triggerAutoPopup) {
    this("", triggerAutoPopup);
  }

  public AddColonSpaceInsertHandler(String ignoreOnChars, boolean triggerAutoPopup) {
    myIgnoreOnChars = ignoreOnChars;
    myTriggerAutoPopup = triggerAutoPopup;
  }

  public void handleInsert(InsertionContext context, LookupElement item) {
    Editor editor = context.getEditor();
    char completionChar = context.getCompletionChar();
    if (completionChar == ' ' || StringUtil.containsChar(myIgnoreOnChars, completionChar)) return;
    Project project = editor.getProject();
    if (project != null) {
      if (!isCharAtColon(editor)) {
        EditorModificationUtil.insertStringAtCaret(editor, ": ");
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
      }
      else {
        editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + 2);
      }
      if (myTriggerAutoPopup) {
        AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
      }
    }
  }

  private static boolean isCharAtColon(Editor editor) {
    final int startOffset = editor.getCaretModel().getOffset();
    final Document document = editor.getDocument();
    return document.getTextLength() > startOffset && document.getCharsSequence().charAt(startOffset) == ':';
  }
}
