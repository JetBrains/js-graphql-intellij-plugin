/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService.GRAPH_QL_QUERY_COMPONENT
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.Key

class GraphQLToggleVariablesAction : ToggleAction(
  GraphQLBundle.message("graphql.action.toggle.variables.editor.title"),
  GraphQLBundle.message("graphql.action.toggle.variables.editor.desc"),
  GraphQLIcons.UI.GraphQLVariables
) {
  companion object {
    private val GRAPH_QL_VARIABLES_MODEL = Key.create<Boolean>("graphql.variables.model")
  }

  override fun update(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE)
    if (editor != null) {
      val endpointsModel = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL)
      e.presentation.isEnabled = endpointsModel?.selectedItem != null
    }
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun isSelected(e: AnActionEvent): Boolean {
    return getVariablesEditor(e)?.getUserData(GRAPH_QL_VARIABLES_MODEL) == true
  }

  override fun setSelected(e: AnActionEvent, state: Boolean) {
    val variablesEditor = getVariablesEditor(e) ?: return
    val queryEditor = variablesEditor.getUserData(GraphQLUIProjectService.GRAPH_QL_QUERY_EDITOR) ?: return
    val scroll = queryEditor.scrollingModel
    val currentScroll = scroll.verticalScrollOffset
    variablesEditor.putUserData(GRAPH_QL_VARIABLES_MODEL, state)
    variablesEditor.getUserData(GRAPH_QL_QUERY_COMPONENT)?.isVisible = state
    if (state) {
      variablesEditor.contentComponent.grabFocus()
    }
    else {
      queryEditor.contentComponent.grabFocus()
    }

    // restore scroll position after the editor has had a chance to re-layout
    invokeLater(ModalityState.NON_MODAL) {
      scroll.scrollVertically(currentScroll)
    }
  }

  private fun getVariablesEditor(e: AnActionEvent): Editor? {
    val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE)
    if (editor != null) {
      val variablesEditor = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_VARIABLES_EDITOR)
      return variablesEditor ?: editor
    }
    return null
  }
}
