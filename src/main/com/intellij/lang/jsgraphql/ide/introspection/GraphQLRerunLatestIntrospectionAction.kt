/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GraphQLRerunLatestIntrospectionAction : AnAction() {
  companion object {
    const val ACTION_ID = "GraphQLRerunLatestIntrospection"
  }

  override fun update(e: AnActionEvent) {
    val project = e.project ?: return
    var enabled = false
    val latestIntrospection = GraphQLIntrospectionService.getInstance(project).latestIntrospection
    if (latestIntrospection != null) {
      enabled = true
      e.presentation.text =
        GraphQLBundle.message("graphql.action.rerun.latest.introspection.schema.title", latestIntrospection.endpoint.displayName)
    }
    e.presentation.isEnabled = enabled
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    GraphQLIntrospectionService.getInstance(project).latestIntrospection?.runnable?.run()
  }
}
