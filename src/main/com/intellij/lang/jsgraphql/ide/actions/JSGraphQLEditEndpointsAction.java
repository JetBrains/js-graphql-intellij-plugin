/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.icons.AllIcons;
import com.intellij.lang.jsgraphql.ide.configuration.JSGraphQLConfigurationProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class JSGraphQLEditEndpointsAction extends AnAction {

    private static final String SETTINGS_TOOLTIP = "Edit the GraphQL endpoints in " + JSGraphQLConfigurationProvider.GRAPHQL_CONFIG_JSON;

    public JSGraphQLEditEndpointsAction() {
        super(SETTINGS_TOOLTIP, SETTINGS_TOOLTIP, AllIcons.General.Settings);
    }

    @Override
    public void update(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        if(project != null) {
            e.getPresentation().setEnabled(JSGraphQLConfigurationProvider.getService(project).hasGraphQLConfig());
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project myProject = e.getData(CommonDataKeys.PROJECT);
        if(myProject != null) {
            VirtualFile config = JSGraphQLConfigurationProvider.getService(myProject).getGraphQLConfigFile();
            if(config != null) {
                final FileEditorManager fileEditorManager = FileEditorManager.getInstance(myProject);
                fileEditorManager.openFile(config, true, true);
                final FileEditor configEditor = fileEditorManager.getSelectedEditor(config);
                if(configEditor instanceof TextEditor) {
                    final TextEditor textEditor = (TextEditor) configEditor;
                    final int endpointsIndex = textEditor.getEditor().getDocument().getText().indexOf("\"endpoints\"");
                    if(endpointsIndex != -1) {
                        textEditor.getEditor().getCaretModel().moveToOffset(endpointsIndex);
                        textEditor.getEditor().getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    }
                }
            }
        }
    }
}
