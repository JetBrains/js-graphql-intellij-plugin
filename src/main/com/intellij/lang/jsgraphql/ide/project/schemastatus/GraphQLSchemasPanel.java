/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus;

import com.intellij.codeInspection.InspectionProfile;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.util.treeView.IndexComparator;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLRerunLatestIntrospectionAction;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.profile.ProfileChangeAdapter;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.SideBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tool window panel that shows the status of the GraphQL schemas discovered in the project.
 */
public class GraphQLSchemasPanel extends JPanel implements Disposable {

    private final Project myProject;
    private final MessageBusConnection myConnection;
    private SimpleTree myTree;

    public GraphQLSchemasPanel(@NotNull Project project) {
        myProject = project;

        myConnection = project.getMessageBus().connect(this);

        setLayout(new BorderLayout());
        add(createToolPanel(), BorderLayout.WEST);
        add(createTreePanel(), BorderLayout.CENTER);
    }

    @Override
    public void dispose() {
    }

    enum TreeUpdate {
        NONE,
        UPDATE,
        REBUILD
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

        final Application application = ApplicationManager.getApplication();

        final SimpleTreeStructure.Impl treeStructure = new SimpleTreeStructure.Impl(new GraphQLSchemasRootNode(myProject));
        final DefaultTreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        final SimpleTreeBuilder myBuilder = new SimpleTreeBuilder(myTree, treeModel, treeStructure, IndexComparator.INSTANCE) {
            @Override
            public boolean isToBuildChildrenInBackground(Object element) {
                // don't block the UI thread when doing schema discovery in the tree
                return application.isDispatchThread();
            }
        };
        Disposer.register(this, myBuilder);

        // queue tree updates for when the user is idle to prevent perf-hit in the editor
        final AtomicReference<TreeUpdate> shouldUpdateTree = new AtomicReference<>(TreeUpdate.NONE);
        final AtomicReference<Integer> currentSchemaVersion = new AtomicReference<>(null);
        final Runnable treeUpdater = () -> {
            final TreeUpdate updateToPerform = shouldUpdateTree.getAndSet(TreeUpdate.NONE);
            if (updateToPerform != TreeUpdate.NONE) {
                final Integer startVersion = currentSchemaVersion.get();
                myBuilder.cancelUpdate().doWhenProcessed(() -> {
                    application.executeOnPooledThread(() -> {
                        // run the schema discovery on a pooled to prevent blocking of the UI thread by asking the nodes for heir child nodes
                        // the schema caches will be ready when the UI thread then needs to show the tree nodes
                        if (myProject.isDisposed()) {
                            return;
                        }
                        try {
                            application.runReadAction(() -> {
                                // use read action to enable use of indexes
                                final GraphQLSchemasRootNode root = (GraphQLSchemasRootNode) treeStructure.getRootElement();
                                for (SimpleNode schemaNode : root.getChildren()) {
                                    for (SimpleNode node : schemaNode.getChildren()) {
                                        node.getChildren();
                                    }
                                }
                                final Integer endVersion = currentSchemaVersion.get();
                                if (startVersion == null || startVersion.equals(endVersion)) {
                                    // initial update or we're still on the same version
                                    // otherwise a new update is pending so don't need to call the updateFromRoot in this thread
                                    myBuilder.updateFromRoot(updateToPerform == TreeUpdate.REBUILD);
                                }
                            });
                        } catch (IndexNotReadyException | ProcessCanceledException ignored) {
                            // allowed to happen here -- retry will run later
                        }
                    });
                });
            }
        };

        // update the tree after being idle for a short while
        IdeEventQueue.getInstance().addIdleListener(treeUpdater, 750);

        Disposer.register(this, () -> IdeEventQueue.getInstance().removeIdleListener(treeUpdater));

        // update tree on schema or config changes
        myConnection.subscribe(GraphQLSchemaChangeListener.TOPIC, (schemaVersion) -> {
            currentSchemaVersion.set(schemaVersion);
            shouldUpdateTree.compareAndSet(TreeUpdate.NONE, TreeUpdate.UPDATE);
        });
        myConnection.subscribe(GraphQLConfigManager.TOPIC, () -> shouldUpdateTree.set(TreeUpdate.REBUILD));


        // update tree in response to indexing changes
        myConnection.subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
            @Override
            public void enteredDumbMode() {
                shouldUpdateTree.set(TreeUpdate.REBUILD);
            }

            @Override
            public void exitDumbMode() {
                shouldUpdateTree.set(TreeUpdate.REBUILD);
            }
        });

        final JBScrollPane scrollPane = new JBScrollPane(myTree);
        scrollPane.setBorder(IdeBorderFactory.createBorder(SideBorder.LEFT));

        // "bold" the schema node that matches the edited file
        myConnection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
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
                        shouldUpdateTree.set(TreeUpdate.UPDATE);
                    }
                }
            }
        });

        myConnection.subscribe(ProfileChangeAdapter.TOPIC, new ProfileChangeAdapter() {
            @Override
            public void profileChanged(@Nullable InspectionProfile profile) {
                shouldUpdateTree.set(TreeUpdate.UPDATE);
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
        leftActionGroup.add(new AnAction("Add Schema Configuration", "Adds a new GraphQL configuration file", AllIcons.General.Add) {
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

        leftActionGroup.add(new AnAction("Edit Selected Schema Configuration", "Opens the .graphqlconfig file for the selected schema", AllIcons.General.Settings) {
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
        leftActionGroup.add(new AnAction("Restart Schema Discovery", "Performs GraphQL schema discovery across the project", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                myProject.getMessageBus().syncPublisher(GraphQLSchemaChangeListener.TOPIC).onGraphQLSchemaChanged(null);
                GraphQLConfigManager.getService(myProject).buildConfigurationModel(null, null);
            }
        });
        leftActionGroup.add(new AnAction("Help", "Open the GraphQL plugin documentation", AllIcons.Actions.Help) {
            @Override
            public void actionPerformed(AnActionEvent e) {
                BrowserUtil.browse("https://github.com/jimkyndemeyer/js-graphql-intellij-plugin");
            }
        });

        final JPanel panel = new JPanel(new BorderLayout());
        final ActionManager actionManager = ActionManager.getInstance();
        final ActionToolbar leftToolbar = actionManager.createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, leftActionGroup, false);
        leftToolbar.setTargetComponent(panel);
        panel.add(leftToolbar.getComponent(), BorderLayout.WEST);
        return panel;
    }

}
