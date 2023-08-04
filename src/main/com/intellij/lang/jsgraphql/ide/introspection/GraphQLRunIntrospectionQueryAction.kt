/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class GraphQLRunIntrospectionQueryAction : AnAction(
  GraphQLBundle.messagePointer("graphql.action.run.introspection.query.title"),
  AllIcons.Actions.Refresh,
) {
  companion object {
    const val ACTION_ID = "GraphQLRunIntrospectionQuery"
  }

  override fun update(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return
    val endpointsModel = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL)
    if (endpointsModel == null || endpointsModel.selectedItem == null) {
      e.presentation.isEnabled = false
      return
    }
    val querying = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_EDITOR_QUERYING) == true
    e.presentation.isEnabled = !querying
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return
    val endpoint = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL)?.selectedItem ?: return
    GraphQLIntrospectionService.getInstance(project).performIntrospectionQuery(endpoint)
  }
}
