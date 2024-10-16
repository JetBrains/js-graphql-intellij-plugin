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
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.actions.GraphQLRestartSchemaDiscoveryAction
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigListener
import com.intellij.lang.jsgraphql.ide.project.toolwindow.GraphQLToolWindow
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaCacheChangeListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentChangeListener
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaContentTracker
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.ActivateToolWindow
import com.intellij.openapi.wm.ex.ToolWindowManagerListener.ToolWindowManagerEventType.HideToolWindow
import com.intellij.profile.ProfileChangeAdapter
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.PopupHandler
import com.intellij.ui.SideBorder
import com.intellij.ui.TreeSpeedSearch
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.tree.AsyncTreeModel
import com.intellij.ui.tree.StructureTreeModel
import com.intellij.ui.treeStructure.AutoExpandSimpleNodeListener
import com.intellij.ui.treeStructure.SimpleTree
import com.intellij.util.Alarm
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.ui.tree.TreeUtil
import java.awt.BorderLayout
import java.awt.Component
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JPanel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

/**
 * Tool window panel that shows the status of the GraphQL schemas discovered in the project.
 */
class GraphQLSchemasPanel(private val project: Project, private val toolWindowId: String) : JPanel(), Disposable {
  private val connection: MessageBusConnection = project.messageBus.connect(this)
  private val schemaModificationTracker = GraphQLSchemaContentTracker.getInstance(project)

  private lateinit var structure: GraphQLSchemaTreeStructure
  private lateinit var model: StructureTreeModel<GraphQLSchemaTreeStructure>
  private lateinit var tree: SimpleTree

  private val updateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)

  private val isInitialized = AtomicBoolean()
  private var scheduleUpdateEvents = true

  init {
    layout = BorderLayout()

    createTree()
    add(createToolPanel(), BorderLayout.WEST)
    add(createTreePanel(), BorderLayout.CENTER)

    subscribeToToolWindowEvents()
  }

  override fun dispose() {
    // disposed by tool window
  }

  private fun createTreePanel(): Component {
    structure = GraphQLSchemaTreeStructure(project)
    model = StructureTreeModel(structure, this)

    val asyncModel = AsyncTreeModel(model, this)
    asyncModel.addTreeModelListener(AutoExpandSimpleNodeListener(tree))
    tree.model = asyncModel

    connection.subscribe(GraphQLSchemaContentChangeListener.TOPIC, object : GraphQLSchemaContentChangeListener {
      override fun onSchemaChanged() {
        updateTree()
      }
    })

    connection.subscribe(GraphQLSchemaCacheChangeListener.TOPIC, object : GraphQLSchemaCacheChangeListener {
      override fun onSchemaCacheChanged() {
        updateTree()
      }
    })

    connection.subscribe(GraphQLConfigListener.TOPIC, object : GraphQLConfigListener {
      override fun onConfigurationChanged() {
        updateTree()
      }
    })

    // update tree in response to indexing changes
    connection.subscribe(DumbService.DUMB_MODE, object : DumbService.DumbModeListener {
      override fun enteredDumbMode() {
        updateTree()
      }

      override fun exitDumbMode() {
        updateTree()
      }
    })

    connection.subscribe(ProfileChangeAdapter.TOPIC, object : ProfileChangeAdapter {
      override fun profileChanged(profile: InspectionProfile) {
        updateTree()
      }
    })

    return JBScrollPane(tree).apply { border = IdeBorderFactory.createBorder(SideBorder.LEFT) }
  }

  private fun subscribeToToolWindowEvents() {
    connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
      override fun stateChanged(toolWindowManager: ToolWindowManager, toolWindow: ToolWindow, changeType: ToolWindowManagerListener.ToolWindowManagerEventType) {
        if (toolWindow.id != toolWindowId) return

        when (changeType) {
          ActivateToolWindow -> {
            scheduleUpdateEvents = true
          }
          HideToolWindow -> {
            scheduleUpdateEvents = false
            updateAlarm.cancelAllRequests()
          }
          else -> {}
        }
      }

      override fun toolWindowShown(toolWindow: ToolWindow) {
        if (toolWindow.id != toolWindowId) return

        scheduleUpdateEvents = true
        updateTree()
      }
    })
  }

  private fun createTree() {
    tree = object : SimpleTree() {
      // tree implementation which only show selection when focused to prevent selection flash during updates (editor changes)
      override fun isRowSelected(i: Int): Boolean {
        return hasFocus() && super.isRowSelected(i)
      }

      override fun isPathSelected(treePath: TreePath): Boolean {
        return hasFocus() && super.isPathSelected(treePath)
      }
    }
    tree.emptyText.text = GraphQLBundle.message("graphql.toolwindow.discovery.not.completed")
    tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
    tree.isRootVisible = false
    tree.showsRootHandles = true
    tree.isLargeModel = true

    TreeUtil.installActions(tree)
    TreeSpeedSearch(tree)

    tree.addMouseListener(object : PopupHandler() {
      override fun invokePopup(comp: Component?, x: Int, y: Int) {
        tree.getClosestPathForLocation(x, y)
          ?.let { tree.getNodeFor(it) as? GraphQLSchemaContextMenuNode }
          ?.handleContextMenu(comp, x, y)
      }
    })
  }

  private fun createToolPanel(): Component {
    val actionManager = ActionManager.getInstance()
    val group = DefaultActionGroup()

    group.add(object : AnAction(
      GraphQLBundle.message("graphql.action.add.schema.configuration.text"),
      GraphQLBundle.message("graphql.action.adds.new.graphql.configuration.file.description"),
      AllIcons.General.Add
    ) {
      override fun actionPerformed(e: AnActionEvent) {
        val dialog = TreeDirectoryChooserDialog(project, GraphQLBundle.message("graphql.dialog.title.select.graphql.schema.base.directory"))
        if (dialog.showAndGet()) {
          val selectedDirectory = dialog.selectedDirectory
          if (selectedDirectory != null) {
            val configFactory = GraphQLConfigFactory.getInstance(project)
            configFactory.createAndOpenConfigFile(selectedDirectory, true)
          }
        }
      }
    })

    group.add(object : AnAction(
      GraphQLBundle.message("graphql.action.edit.selected.schema.configuration.text"),
      GraphQLBundle.message("graphql.action.opens.graphql.config.file.for.selected.schema.description"),
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
      group.add(it)
    }

    group.add(object : AnAction(
      GraphQLBundle.message("graphql.action.schemas.panel.help.text"),
      GraphQLBundle.message("graphql.action.schemas.panel.open.documentation.description"),
      AllIcons.Actions.Help
    ) {
      override fun actionPerformed(e: AnActionEvent) {
        BrowserUtil.browse("https://github.com/JetBrains/js-graphql-intellij-plugin")
      }
    })

    val actionsManager = CommonActionsManager.getInstance()
    val treeExpander = DefaultTreeExpander(tree)
    group.addSeparator()
    group.add(actionsManager.createExpandAllAction(treeExpander, tree))
    group.add(actionsManager.createCollapseAllAction(treeExpander, tree))

    val toolbar =
      actionManager.createActionToolbar(GraphQLToolWindow.GRAPHQL_TOOL_WINDOW_TOOLBAR, group, false)
    toolbar.targetComponent = this
    return toolbar.component
  }

  private fun updateTree() {
    if (!scheduleUpdateEvents) {
      return
    }

    val startVersion = schemaModificationTracker.modificationCount

    updateAlarm.cancelAllRequests()
    updateAlarm.addRequest(
      {
        // run the schema discovery on a pooled to prevent blocking of the UI thread by asking the nodes for heir child nodes
        // the schema caches will be ready when the UI thread then needs to show the tree nodes
        if (project.isDisposed) {
          return@addRequest
        }

        try {
          val currentVersion = schemaModificationTracker.modificationCount
          if (isInitialized.compareAndSet(false, true) || startVersion == currentVersion) {
            model.invalidateAsync()
          }
        }
        catch (_: IndexNotReadyException) {
          // allowed to happen here -- retry will run later
        }
        catch (_: ProcessCanceledException) {
        }
      }, 750, ModalityState.stateForComponent(this)
    )
  }
}
