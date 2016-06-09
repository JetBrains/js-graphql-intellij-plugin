/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.toolwindow;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.ide.actions.CloseTabToolbarAction;
import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Tree view panel showing current GraphQL errors.
 */
public class JSGraphQLErrorTreeViewPanel extends NewErrorTreeViewPanel {
    private final String myHelpId;
    private final Runnable myCleanAction;
    @Nullable
    private final AnAction[] myActions;
    private TreeExpander myTreeExpander;

    JSGraphQLErrorTreeViewPanel(Project project, String helpId, @Nullable Runnable cleanAction, @Nullable AnAction... actions) {
        super(project, helpId, false, false);
        myHelpId = helpId;
        myCleanAction = cleanAction;
        myActions = actions;
        myTreeExpander = (TreeExpander) getData(PlatformDataKeys.TREE_EXPANDER.getName());
        add(createToolPanel(), BorderLayout.WEST);
        myTree.getEmptyText().setText("No Errors");
    }

    private Component createToolPanel() {
        CloseTabToolbarAction closeMessageViewAction = new CloseTabToolbarAction() {
            public void actionPerformed(AnActionEvent e) {
                close();
            }
        };
        DefaultActionGroup leftActionGroup = new DefaultActionGroup();
        DefaultActionGroup rightActionGroup = new DefaultActionGroup();
        if (myTreeExpander != null) {
            rightActionGroup.add(CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this));
            rightActionGroup.add(CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, this));
        }

        rightActionGroup.add(new DumbAwareAction("Clear All", null, Actions.GC) {
            public void actionPerformed(AnActionEvent e) {
                if (myCleanAction != null) {
                    myCleanAction.run();
                }
                getErrorViewStructure().clear();
                updateTree();
            }
        });
        if (myActions != null) {
            for (AnAction rightToolbar : myActions) {
                leftActionGroup.add(rightToolbar);
            }
        }

        leftActionGroup.add(new ContextHelpAction(myHelpId));
        leftActionGroup.add(closeMessageViewAction);
        final JPanel panel = new JPanel(new BorderLayout());
        final ActionManager actionManager = ActionManager.getInstance();
        final ActionToolbar leftToolbar = actionManager.createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, leftActionGroup, false);
        final ActionToolbar rightToolbar = actionManager.createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, rightActionGroup, false);
        panel.add(leftToolbar.getComponent(), BorderLayout.WEST);
        panel.add(rightToolbar.getComponent(), BorderLayout.CENTER);
        return panel;
    }
}
