/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.codeInspection.InspectionProfile
import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.util.treeView.IndexComparator
import com.intellij.lang.jsgraphql.ide.actions.GraphQLRestartSchemaDiscoveryAction
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentChangeListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Disposer
import com.intellij.profile.ProfileChangeAdapter
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.ui.treeStructure.SimpleTreeBuilder
import com.intellij.ui.treeStructure.SimpleTreeStructure
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.tree.TreeUtil
import java.awt.BorderLayout
import java.awt.Component
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * Tool window panel that shows the status of the GraphQL schemas discovered in the project.
 */
class GraphQLSchemasPanel(private val project: Project) : JPanel(), Disposable {
    private val myConnection: MessageBusConnection = project.messageBus.connect(this)
    private val mySchemaModificationTracker = GraphQLSchemaContentTracker.getInstance(project)
    private lateinit var tree: SimpleTree

    init {
        layout = BorderLayout()
        add(createToolPanel(), BorderLayout.WEST)
        add(createTreePanel(), BorderLayout.CENTER)
    }

    override fun dispose() {
        // disposed by tool window
    }

    internal enum class TreeUpdate {
        NONE,
        UPDATE,
        REBUILD
    }

    private fun createTreePanel(): Component {
        tree = object : SimpleTree() {
            // tree implementation which only show selection when focused to prevent selection flash during updates (editor changes)
            override fun isRowSelected(i: Int): Boolean {
                return hasFocus() && super.isRowSelected(i)
            }

            override fun isPathSelected(treePath: TreePath): Boolean {
                return hasFocus() && super.isPathSelected(treePath)
            }
        }
        tree.emptyText.setText("Schema discovery has not completed.")
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        tree.isRootVisible = false
        tree.showsRootHandles = true
        tree.isLargeModel = true

        TreeUtil.installActions(tree)

        val application = ApplicationManager.getApplication()
        val treeStructure = SimpleTreeStructure.Impl(GraphQLSchemasRootNode(project))
        val treeModel = DefaultTreeModel(DefaultMutableTreeNode())
        val myBuilder: SimpleTreeBuilder =
            object : SimpleTreeBuilder(tree, treeModel, treeStructure, IndexComparator.INSTANCE) {
                override fun isToBuildChildrenInBackground(element: Any): Boolean {
                    // don't block the UI thread when doing schema discovery in the tree
                    return application.isDispatchThread
                }
            }

        Disposer.register(this, myBuilder)

        // queue tree updates for when the user is idle to prevent perf-hit in the editor
        val shouldUpdateTree = AtomicReference(TreeUpdate.NONE)
        val isInitialized = AtomicBoolean()
        val treeUpdater = Runnable {
            val updateToPerform = shouldUpdateTree.getAndSet(TreeUpdate.NONE)
            if (updateToPerform != TreeUpdate.NONE) {
                val startVersion = mySchemaModificationTracker.modificationCount
                myBuilder.cancelUpdate().doWhenProcessed {
                    application.executeOnPooledThread {
                        // run the schema discovery on a pooled to prevent blocking of the UI thread by asking the nodes for heir child nodes
                        // the schema caches will be ready when the UI thread then needs to show the tree nodes
                        if (project.isDisposed) {
                            return@executeOnPooledThread
                        }
                        try {
                            runReadAction {
                                // use read action to enable use of indexes
                                val root = treeStructure.rootElement as GraphQLSchemasRootNode
                                for (schemaNode in root.children) {
                                    for (node in schemaNode.children) {
                                        node.children
                                    }
                                }
                                val endVersion = mySchemaModificationTracker.modificationCount
                                if (isInitialized.compareAndSet(false, true) || startVersion == endVersion) {
                                    // initial update or we're still on the same version
                                    // otherwise a new update is pending so don't need to call the updateFromRoot in this thread
                                    myBuilder.updateFromRoot(updateToPerform == TreeUpdate.REBUILD)
                                }
                            }
                        } catch (ignored: IndexNotReadyException) {
                            // allowed to happen here -- retry will run later
                        } catch (ignored: ProcessCanceledException) {
                        }
                    }
                }
            }
        }

        // update the tree after being idle for a short while
        IdeEventQueue.getInstance().addIdleListener(treeUpdater, 750)
        Disposer.register(this) { IdeEventQueue.getInstance().removeIdleListener(treeUpdater) }

        // update tree on schema or config changes
        myConnection.subscribe(GraphQLSchemaContentChangeListener.TOPIC,
            object : GraphQLSchemaContentChangeListener {
                override fun onSchemaChanged() {
                    shouldUpdateTree.compareAndSet(TreeUpdate.NONE, TreeUpdate.UPDATE)
                }
            })

        myConnection.subscribe(
            GraphQLConfigListener.TOPIC,
            object : GraphQLConfigListener {
                override fun onConfigurationChanged() {
                    shouldUpdateTree.set(TreeUpdate.REBUILD)
                }
            }
        )

        // update tree in response to indexing changes
        myConnection.subscribe(
            DumbService.DUMB_MODE,
            object : DumbService.DumbModeListener {
                override fun enteredDumbMode() {
                    shouldUpdateTree.set(TreeUpdate.REBUILD)
                }

                override fun exitDumbMode() {
                    shouldUpdateTree.set(TreeUpdate.REBUILD)
                }
            })

        val scrollPane = JBScrollPane(tree)
        scrollPane.border = IdeBorderFactory.createBorder(SideBorder.LEFT)

        // "bold" the schema node that matches the edited file
        myConnection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun selectionChanged(event: FileEditorManagerEvent) {
                    if (event.newFile != null) {
                        val schemaNode =
                            findNode(treeModel.root as DefaultMutableTreeNode) { node: DefaultMutableTreeNode ->
                                if (node.userObject is GraphQLConfigSchemaNode) {
                                    return@findNode (node.userObject as GraphQLConfigSchemaNode).representsFile(event.newFile)
                                }
                                false
                            }
                        if (schemaNode != null) {
                            shouldUpdateTree.set(TreeUpdate.UPDATE)
                        }
                    }
                }
            })

        myConnection.subscribe(ProfileChangeAdapter.TOPIC, object : ProfileChangeAdapter {
            override fun profileChanged(profile: InspectionProfile) {
                shouldUpdateTree.set(TreeUpdate.UPDATE)
            }
        })

        return scrollPane
    }

    private fun createToolPanel(): Component {
        val actionManager = ActionManager.getInstance()
        val leftActionGroup = DefaultActionGroup()

        leftActionGroup.add(object : AnAction(
            "Add Schema Configuration",
            "Adds a new GraphQL configuration file",
            AllIcons.General.Add
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                val dialog = TreeDirectoryChooserDialog(project, "Select GraphQL Schema Base Directory")
                if (dialog.showAndGet()) {
                    val selectedDirectory = dialog.selectedDirectory
                    if (selectedDirectory != null) {
                        val configFactory = GraphQLConfigFactory.getInstance(project)
                        configFactory.createAndOpenConfigFile(selectedDirectory, true)
                    }
                }
            }
        })

        leftActionGroup.add(object : AnAction(
            "Edit Selected Schema Configuration",
            "Opens the GraphQL config file for the selected schema",
            AllIcons.General.Settings
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                val selectedSchemaNode = selectedSchemaNode
                if (selectedSchemaNode != null) {
                    val configFile = selectedSchemaNode.configFile
                    if (configFile != null) {
                        FileEditorManager.getInstance(project).openFile(configFile, true)
                    }
                }
            }

            override fun update(e: AnActionEvent) {
                e.presentation.isEnabled = selectedSchemaNode != null
            }

            override fun getActionUpdateThread(): ActionUpdateThread {
                return ActionUpdateThread.EDT
            }

            private val selectedSchemaNode: GraphQLConfigSchemaNode?
                get() {
                    var node = tree.selectedNode
                    while (node != null) {
                        if (node is GraphQLConfigSchemaNode) {
                            return node
                        }
                        node = node.parent
                    }
                    return null
                }
        })

        actionManager.getAction(GraphQLRestartSchemaDiscoveryAction.ACTION_ID)?.let {
            leftActionGroup.add(it)
        }

        leftActionGroup.add(object : AnAction(
            "Help",
            "Open the GraphQL plugin documentation",
            AllIcons.Actions.Help
        ) {
            override fun actionPerformed(e: AnActionEvent) {
                BrowserUtil.browse("https://github.com/JetBrains/js-graphql-intellij-plugin")
            }
        })

        val leftToolbar =
            actionManager.createActionToolbar(ActionPlaces.COMPILER_MESSAGES_TOOLBAR, leftActionGroup, false)
        leftToolbar.targetComponent = this
        return leftToolbar.component
    }

    companion object {
        private fun findNode(
            root: DefaultMutableTreeNode,
            condition: Condition<DefaultMutableTreeNode>,
        ): DefaultMutableTreeNode? {
            return if (condition.value(root)) {
                root
            } else {
                for (i in 0 until root.childCount) {
                    val candidate =
                        findNode(root.getChildAt(i) as DefaultMutableTreeNode, condition)
                    if (candidate != null) {
                        return candidate
                    }
                }

                null
            }
        }
    }
}
