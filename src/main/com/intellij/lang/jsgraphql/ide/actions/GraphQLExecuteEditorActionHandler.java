/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Control+Enter is "split line" by default in IDEA and the GraphQL editor uses that binding to execute queries for .graphqil files.
 * This EditorActionHandler stops that default behavior and runs the "execute GraphQL" action.
 */
public final class GraphQLExecuteEditorActionHandler extends EditorActionHandler {

  private final EditorActionHandler myDelegate;

  public GraphQLExecuteEditorActionHandler(EditorActionHandler delegate) {
    myDelegate = delegate;
  }

  @Override
  protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
    PsiFile file = dataContext.getData(LangDataKeys.PSI_FILE);
    if (file instanceof GraphQLFile || isQueryVariablesFile(dataContext)) {
      final AnAction executeGraphQLAction = ActionManager.getInstance().getAction(GraphQLExecuteEditorAction.ACTION_ID);
      final AnActionEvent actionEvent = AnActionEvent.createFromInputEvent(null, ActionPlaces.EDITOR_TOOLBAR,
                                                                           executeGraphQLAction.getTemplatePresentation(), dataContext);
      executeGraphQLAction.actionPerformed(actionEvent);
      return;
    }
    myDelegate.execute(editor, caret, dataContext);
  }

  private boolean isQueryVariablesFile(DataContext dataContext) {
    final VirtualFile virtualFile = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
    return virtualFile != null && Boolean.TRUE.equals(
      virtualFile.getUserData(GraphQLUIProjectService.IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE));
  }
}
