/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.actions;

import com.intellij.ide.IdeEventQueue;
import com.intellij.lang.jsgraphql.ide.project.GraphQLUIProjectService;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.keymap.impl.IdeKeyEventDispatcher;
import com.intellij.openapi.keymap.impl.KeyProcessorContext;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.awt.event.InputEvent;

/**
 * Control+Enter is "split line" by default in IDEA and the JS GraphQL editor uses that binding to execute queries for .graphqil files.
 * This EditorActionHandler stops that default behavior and runs the "execute GraphQL" action.
 */
public class JSGraphQLExecuteEditorActionHandler extends EditorActionHandler {

    private final EditorActionHandler myOriginalHandler;

    public JSGraphQLExecuteEditorActionHandler(EditorActionHandler originalHandler) {
        this.myOriginalHandler = originalHandler;
    }

    @Override
    protected void doExecute(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        final Object file = dataContext.getData(LangDataKeys.PSI_FILE.getName());
        if(file instanceof GraphQLFile || isQueryVariablesFile(dataContext)) {
            final InputEvent event = getKeyboardEvent();
            if(event != null) {
                final AnAction executeGraphQLAction = ActionManager.getInstance().getAction(JSGraphQLExecuteEditorAction.class.getName());
                final AnActionEvent actionEvent = AnActionEvent.createFromInputEvent(event, ActionPlaces.EDITOR_TOOLBAR, executeGraphQLAction.getTemplatePresentation(), dataContext);
                executeGraphQLAction.actionPerformed(actionEvent);
                return;
            }
        }
        myOriginalHandler.execute(editor, caret, dataContext);
    }

    private boolean isQueryVariablesFile(DataContext dataContext) {
        final VirtualFile virtualFile = (VirtualFile)dataContext.getData(CommonDataKeys.VIRTUAL_FILE.getName());
        if(virtualFile != null && Boolean.TRUE.equals(virtualFile.getUserData(GraphQLUIProjectService.IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE))) {
            return true;
        }
        return false;
    }

    private InputEvent getKeyboardEvent() {
        final IdeKeyEventDispatcher keyEventDispatcher = IdeEventQueue.getInstance().getKeyEventDispatcher();
        if (keyEventDispatcher != null) {
            final KeyProcessorContext context = keyEventDispatcher.getContext();
            if (context != null) {
                return context.getInputEvent();
            }
        }
        return null;
    }

}
