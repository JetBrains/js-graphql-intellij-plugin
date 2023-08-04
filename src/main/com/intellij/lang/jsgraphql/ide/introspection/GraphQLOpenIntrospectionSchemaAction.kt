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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.LocalFileSystem

class GraphQLOpenIntrospectionSchemaAction : AnAction(
  GraphQLBundle.messagePointer("graphql.action.open.introspection.schema.title"),
  AllIcons.Actions.MoveTo2,
) {
  companion object {
    const val ACTION_ID = "GraphQLOpenIntrospectionSchema"
  }

  override fun update(e: AnActionEvent) {
    val editor = e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE) ?: return
    val endpoint = editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL)?.selectedItem
    e.presentation.isEnabled = endpoint != null && endpoint.schemaPointer?.outputPath != null
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.EDT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return

    e.getData(CommonDataKeys.EDITOR_EVEN_IF_INACTIVE)
      ?.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL)
      ?.selectedItem
      ?.schemaPointer
      ?.outputPath
      ?.let { LocalFileSystem.getInstance().findFileByPath(it) }
      ?.let { FileEditorManager.getInstance(project).openFile(it, true) }
  }
}
