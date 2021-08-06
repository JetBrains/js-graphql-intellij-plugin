/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.actions;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.project.GraphQLUIProjectService;
import com.intellij.lang.jsgraphql.v1.ide.endpoints.JSGraphQLEndpointsModel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class JSGraphQLExecuteEditorAction extends AnAction {

    public JSGraphQLExecuteEditorAction() {
        super("Execute GraphQL", "Executes the current GraphQL file against the specified GraphQL endpoint", AllIcons.Actions.Execute);
    }

    @Override
    public void update(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if(editor != null) {
            final JSGraphQLEndpointsModel endpointsModel = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL);
            if(endpointsModel == null || endpointsModel.getSelectedItem() == null) {
                e.getPresentation().setEnabled(false);
                return;
            }
            final Boolean querying = Boolean.TRUE.equals(editor.getUserData(GraphQLUIProjectService.GRAPH_QL_EDITOR_QUERYING));
            e.getPresentation().setEnabled(!querying);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if(isQueryableFile(project, virtualFile)) {
            Editor editor = e.getData(CommonDataKeys.EDITOR);
            if(project != null && editor instanceof EditorEx) {
                final Boolean querying = Boolean.TRUE.equals(editor.getUserData(GraphQLUIProjectService.GRAPH_QL_EDITOR_QUERYING));
                if(querying) {
                    // already doing a query
                    return;
                }
                final Editor queryEditor = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_QUERY_EDITOR);
                if(queryEditor != null) {
                    // this action comes from the variables editor, so we need to resolve the query editor which contains the GraphQL
                    editor = queryEditor;
                    virtualFile = CommonDataKeys.VIRTUAL_FILE.getData(((EditorEx)editor).getDataContext());
                }
                GraphQLUIProjectService.getService(project).executeGraphQL(editor, virtualFile);
            }
        }
    }

    private boolean isQueryableFile(Project project, VirtualFile virtualFile) {
        if(virtualFile != null) {
            if(virtualFile.getFileType() == GraphQLFileType.INSTANCE) {
                return true;
            }
            if(GraphQLFileType.isGraphQLScratchFile(project, virtualFile)) {
                return true;
            }
            if(virtualFile.getFileType() == JsonFileType.INSTANCE && Boolean.TRUE.equals(virtualFile.getUserData(GraphQLUIProjectService.IS_GRAPH_QL_VARIABLES_VIRTUAL_FILE))) {
                return true;
            }
        }
        return false;
    }
}
