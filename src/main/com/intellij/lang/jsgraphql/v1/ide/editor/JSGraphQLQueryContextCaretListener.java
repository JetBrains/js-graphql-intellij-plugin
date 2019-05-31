/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.editor;

import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Updates the current caret position in GraphQL files to enable contextual queries and highlighting of included fragments
 */
public class JSGraphQLQueryContextCaretListener implements StartupActivity, DumbAware {

    static final Key<Integer> CARET_OFFSET = Key.create("JSGraphQL.QueryContext.CaretOffset");

    @Override
    public void runActivity(@NotNull Project project) {
        if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            final EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
            final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            eventMulticaster.addCaretListener(new CaretListener() {
                @Override
                public void caretPositionChanged(CaretEvent e) {
                    final PsiFile psiFile = psiDocumentManager.getPsiFile(e.getEditor().getDocument());
                    if (psiFile instanceof GraphQLFile) {
                        int offset = e.getEditor().logicalPositionToOffset(e.getNewPosition());
                        psiFile.putUserData(CARET_OFFSET, offset);
                    }
                }
            }, project);
        }
    }
}
