/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions;

import com.intellij.lang.jsgraphql.icons.GraphQLIcons;
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLEndpointsModel;
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollingModel;
import com.intellij.openapi.util.Key;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

public class GraphQLToggleVariablesAction extends ToggleAction {

    public final static Key<Boolean> GRAPH_QL_VARIABLES_MODEL = Key.create("JSGraphQLVariablesModel");

    public GraphQLToggleVariablesAction() {
        super("Toggle variables editor", "Toggles the GraphQL variables editor", GraphQLIcons.UI.GraphQLVariables);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor != null) {
            final GraphQLEndpointsModel endpointsModel = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL);
            e.getPresentation().setEnabled(endpointsModel != null && endpointsModel.getSelectedItem() != null);
        }
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        final Editor editor = getVariablesEditor(e);
        if (editor != null) {
            return Boolean.TRUE.equals(editor.getUserData(GRAPH_QL_VARIABLES_MODEL));
        }
        return false;
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {

        final Editor variablesEditor = getVariablesEditor(e);
        if (variablesEditor != null) {

            final Editor queryEditor = variablesEditor.getUserData(GraphQLUIProjectService.GRAPH_QL_QUERY_EDITOR);
            if (queryEditor == null) {
                // not linked to a query editor
                return;
            }

            final ScrollingModel scroll = queryEditor.getScrollingModel();
            final int currentScroll = scroll.getVerticalScrollOffset();

            variablesEditor.putUserData(GRAPH_QL_VARIABLES_MODEL, state ? Boolean.TRUE : Boolean.FALSE);
            variablesEditor.getComponent().setVisible(state);

            if (state) {
                variablesEditor.getContentComponent().grabFocus();
            } else {
                queryEditor.getContentComponent().grabFocus();
            }

            // restore scroll position after the editor has had a chance to re-layout
            ApplicationManager.getApplication().invokeLater(() -> {
                UIUtil.invokeLaterIfNeeded(() -> scroll.scrollVertically(currentScroll));
            });

        }

    }

    private Editor getVariablesEditor(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor != null) {
            final Editor variablesEditor = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_VARIABLES_EDITOR);
            return variablesEditor != null ? variablesEditor : editor;
        }
        return null;
    }
}
