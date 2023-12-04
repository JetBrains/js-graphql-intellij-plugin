/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.createScratchFromEndpoint
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.lang.jsgraphql.ide.project.toolwindow.GraphQLToolWindow
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import com.intellij.ui.treeStructure.CachingSimpleNode
import com.intellij.ui.treeStructure.SimpleNode
import com.intellij.ui.treeStructure.SimpleTree
import java.awt.Component
import java.awt.event.InputEvent
import java.awt.event.MouseEvent

/**
 * Tree node which provides a schema endpoints list
 */
class GraphQLSchemaEndpointsListNode(
  parent: SimpleNode?,
  projectConfig: GraphQLProjectConfig?,
) : CachingSimpleNode(parent) {

  private val endpoints: List<GraphQLConfigEndpoint> = projectConfig?.endpoints ?: emptyList()

  init {
    myName = GraphQLBundle.message("graphql.toolwindow.schema.endpoints.list.node.name")
    icon = AllIcons.Nodes.WebFolder
  }

  public override fun buildChildren(): Array<SimpleNode> {
    return if (endpoints.isEmpty()) {
      arrayOf(DefaultEndpointNode(myProject))
    }
    else {
      endpoints
        .map { ConfigurableEndpointNode(this, it) }
        .toTypedArray()
    }
  }

  override fun isAutoExpandNode(): Boolean {
    return true
  }

  private class ConfigurableEndpointNode(
    parent: SimpleNode,
    private val endpoint: GraphQLConfigEndpoint,
  ) : SimpleNode(parent), GraphQLSchemaContextMenuNode {
    init {
      myName = endpoint.displayName
      templatePresentation.tooltip = GraphQLBundle.message("graphql.tooltip.endpoints.perform.introspection.queries.mutations")
      if (!endpoint.url.equals(endpoint.displayName, true)) {
        templatePresentation.locationString = endpoint.url
      }
      icon = GraphQLIcons.UI.GraphQLNode
    }

    override fun handleDoubleClickOrEnter(tree: SimpleTree, inputEvent: InputEvent) {
      if (inputEvent is MouseEvent) {
        showPopup(inputEvent.component, inputEvent.x, inputEvent.y)
      }
    }

    override fun handleContextMenu(component: Component?, x: Int, y: Int) {
      showPopup(component, x, y)
    }

    private fun showPopup(component: Component?, x: Int, y: Int) {
      val group = DefaultActionGroup()
      group.add(object : AnAction(
        GraphQLBundle.messagePointer("graphql.toolwindow.action.introspect.endpoint"),
        AllIcons.Actions.Refresh,
      ) {
        override fun actionPerformed(e: AnActionEvent) {
          GraphQLIntrospectionService.getInstance(myProject).performIntrospectionQuery(endpoint)
        }
      })

      group.add(object : AnAction(
        GraphQLBundle.messagePointer("graphql.toolwindow.action.create.scratch"),
        GraphQLIcons.Files.GraphQLScratch,
      ) {
        override fun actionPerformed(e: AnActionEvent) {
          createScratchFromEndpoint(myProject, endpoint, true)
        }
      })

      val popupMenu =
        ActionManager.getInstance().createActionPopupMenu(GraphQLToolWindow.GRAPHQL_TOOL_WINDOW_POPUP, group)
      popupMenu.component.show(component, x, y)
    }

    override fun getChildren(): Array<SimpleNode> {
      return NO_CHILDREN
    }

    override fun isAlwaysLeaf(): Boolean {
      return true
    }
  }

  private class DefaultEndpointNode(project: Project) : SimpleNode(project) {
    init {
      myName = GraphQLBundle.message("graphql.toolwindow.schema.endpoints.default.node.name")
      templatePresentation.tooltip = GraphQLBundle.message("graphql.tooltip.endpoints.perform.introspection.queries.mutations")
      icon = AllIcons.General.Information
    }

    override fun getChildren(): Array<SimpleNode> {
      return NO_CHILDREN
    }

    override fun isAlwaysLeaf(): Boolean {
      return true
    }
  }
}
