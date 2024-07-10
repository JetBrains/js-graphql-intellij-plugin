/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLEndpointsModel;
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraphQLExecuteEditorAction extends AnAction {
  public static final String ACTION_ID = "GraphQLExecuteEditor";

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
    if (editor != null) {
      final GraphQLEndpointsModel endpointsModel = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL);
      if (endpointsModel == null || endpointsModel.getSelectedItem() == null) {
        e.getPresentation().setEnabled(false);
        return;
      }
      boolean querying = Boolean.TRUE.equals(editor.getUserData(GraphQLUIProjectService.GRAPH_QL_EDITOR_QUERYING));
      e.getPresentation().setEnabled(!querying);
    }
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    final Project project = e.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      return;
    }
    if (!isQueryableFile(project, virtualFile)) {
      return;
    }
    Editor editor = e.getData(CommonDataKeys.EDITOR);
    if (!(editor instanceof EditorEx)) {
      return;
    }

    boolean querying = Boolean.TRUE.equals(editor.getUserData(GraphQLUIProjectService.GRAPH_QL_EDITOR_QUERYING));
    if (querying) {
      // already doing a query
      return;
    }
    Editor queryEditor = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_QUERY_EDITOR);
    if (queryEditor != null) {
      // this action comes from the variables editor, so we need to resolve the query editor which contains the GraphQL
      editor = queryEditor;
      virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(((EditorEx)editor).getDataContext());
    }
    if (virtualFile == null) {
      return;
    }
    GraphQLUIProjectService.getInstance(project).executeGraphQL(editor, virtualFile);
  }

  private static boolean isQueryableFile(@NotNull Project project, @Nullable VirtualFile virtualFile) {
    if (virtualFile == null) {
      return false;
    }
    if (virtualFile.getFileType() == GraphQLFileType.INSTANCE) {
      return true;
    }
    if (GraphQLFileType.isGraphQLScratchFile(virtualFile)) {
      return true;
    }
    return virtualFile.getFileType() == JsonFileType.INSTANCE && Boolean.TRUE.equals(
      virtualFile.getUserData(GraphQLUIProjectService.IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE));
  }
}
