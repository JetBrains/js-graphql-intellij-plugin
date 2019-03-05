/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ContextHelpAction;
import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLRerunLatestIntrospectionAction;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeListener;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.Alarm;
import com.intellij.util.AlarmFactory;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * Tool window panel that shows the status of the GraphQL schemas discovered in the project.
 */
public class GraphQLSchemasPanel extends JPanel {

    private final Project myProject;
    private SimpleTree myTree;

    public GraphQLSchemasPanel(Project project) {
        setLayout(new BorderLayout());
        myProject = project;
        add(createToolPanel(), BorderLayout.WEST);
        add(createTreePanel(), BorderLayout.CENTER);
    }

    private Component createTreePanel() {

        myTree = new SimpleTree() {

            // tree implementation which only show selection when focused to prevent selection flash during updates (editor changes)

            @Override
            public boolean isRowSelected(int i) {
                return hasFocus() && super.isRowSelected(i);
            }

            @Override
            public boolean isPathSelected(TreePath treePath) {
                return hasFocus() && super.isPathSelected(treePath);
            }

        };

        myTree.getEmptyText().setText("Schema discovery has not completed.");
        myTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        myTree.setRootVisible(false);
        myTree.setShowsRootHandles(true);
        myTree.setLargeModel(true);

        TreeUtil.installActions(myTree);
        UIUtil.setLineStyleAngled(myTree);

        SimpleTreeStructure.Impl treeStructure = new SimpleTreeStructure.Impl(new GraphQLSchemasRootNode(myProject));
        DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        SimpleTreeBuilder myBuilder = new SimpleTreeBuilder(myTree, treeModel, treeStructure, IndexComparator.INSTANCE);

        // debounce tree updates to prevent perf-hit
        final Alarm treeUpdaterAlarm = AlarmFactory.getInstance().create();
        final Runnable treeUpdater = () -> myBuilder.updateFromRoot(true);
        final Runnable queueTreeUpdater = () -> {
            treeUpdaterAlarm.cancelRequest(treeUpdater);
            treeUpdaterAlarm.addRequest(treeUpdater, 300); // delay similar to inspection delay
        };

        // update tree on schema or config changes
        final MessageBusConnection connection = myProject.getMessageBus().connect();
        connection.subscribe(GraphQLSchemaChangeListener.TOPIC, queueTreeUpdater::run);
        connection.subscribe(GraphQLConfigManager.TOPIC, queueTreeUpdater::run);

        // Need indexing to be ready to build schema status
        DumbServiceImpl.getInstance(myProject).smartInvokeLater(myBuilder::initRootNode);

        // update tree in response to indexing changes
        connection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
            @Override
            public void enteredDumbMode() {
                myBuilder.updateFromRoot(true);
            }

            @Override
            public void exitDumbMode() {
                myBuilder.updateFromRoot(true);
            }
        });

        final JBScrollPane scrollPane = new JBScrollPane(myTree);
        scrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.LEFT));

        // "bold" the schema node that matches the edited file
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                if (event.getNewFile() != null) {
                    final DefaultMutableTreeNode schemaNode = findNode((DefaultMutableTreeNode) treeModel.getRoot(), node -> {
                        if (node.getUserObject() instanceof GraphQLConfigSchemaNode) {
                            return ((GraphQLConfigSchemaNode) node.getUserObject()).representsFile(event.getNewFile());
                        }
                        return false;
                    });
                    if (schemaNode != null) {
                        myBuilder.updateFromRoot(false); // re-renders from GraphQLConfigSchemaNode.update() being called
                    }
                }
            }
        });

        return scrollPane;
    }

    private static DefaultMutableTreeNode findNode(@NotNull final DefaultMutableTreeNode aRoot,
                                                   @NotNull final Condition<DefaultMutableTreeNode> condition) {
        if (condition.value(aRoot)) {
            return aRoot;
        } else {
            for (int i = 0; i < aRoot.getChildCount(); i++) {
                final DefaultMutableTreeNode candidate = findNode((DefaultMutableTreeNode) aRoot.getChildAt(i), condition);
                if (null != candidate) {
                    return candidate;
                }
            }
            return null;
        }
    }

    private Component createToolPanel() {
        DefaultActionGroup leftActionGroup = new DefaultActionGroup();
        leftActionGroup.add(new AnAction("Add schema configuration", "Adds a new GraphQL configuration file", AllIcons.General.Add) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                final TreeDirectoryChooserDialog dialog = new TreeDirectoryChooserDialog(myProject, "Select GraphQL Schema Base Directory");
                if (dialog.showAndGet()) {
                    if (dialog.getSelectedDirectory() != null) {
                        final GraphQLConfigManager configManager = GraphQLConfigManager.getService(myProject);
                        configManager.createAndOpenConfigFile(dialog.getSelectedDirectory(), true);
                        ApplicationManager.getApplication().saveAll();
                    }
                }
            }
        });

        final AnAction reRunAction = ActionManager.getInstance().getAction(GraphQLRerunLatestIntrospectionAction.class.getName());
        if (reRunAction != null) {
            leftActionGroup.add(reRunAction);
        }

        leftActionGroup.add(new AnAction("Edit selected schema configuration", "Opens the .graphqlconfig file for the selected schema", AllIcons.General.Settings) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                GraphQLConfigSchemaNode selectedSchemaNode = getSelectedSchemaNode();
                if (selectedSchemaNode != null && selectedSchemaNode.getConfigFile() != null) {
                    FileEditorManager.getInstance(myProject).openFile(selectedSchemaNode.getConfigFile(), true);
                }
            }

            @Override
            public void update(AnActionEvent e) {
                e.getPresentation().setEnabled(getSelectedSchemaNode() != null);
            }

            private GraphQLConfigSchemaNode getSelectedSchemaNode() {
                SimpleNode node = myTree.getSelectedNode();
                while (node != null) {
                    if (node instanceof GraphQLConfigSchemaNode) {
                        return (GraphQLConfigSchemaNode) node;
                    }
                    node = node.getParent();
                }
                return null;
            }
        });
        leftActionGroup.add(new AnAction("Restart schema discovery", "Performs GraphQL schema discovery across the project", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                myProject.getMessageBus().syncPublisher(GraphQLSchemaChangeListener.TOPIC).onGraphQLSchemaChanged();
                GraphQLConfigManager.getService(myProject).buildConfigurationModel(null, null);
            }
        });
        leftActionGroup.add(new ContextHelpAction(""));

        final JPanel panel = new JPanel(new BorderLayout());
        final ActionManager actionManager = ActionManager.getInstance();
        final ActionToolbar leftToolbar = actionManager.createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, leftActionGroup, false);
        panel.add(leftToolbar.getComponent(), BorderLayout.WEST);
        return panel;
    }

}
