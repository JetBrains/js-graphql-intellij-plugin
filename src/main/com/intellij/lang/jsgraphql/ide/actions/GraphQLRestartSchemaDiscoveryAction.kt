package com.intellij.lang.jsgraphql.ide.actions

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GraphQLRestartSchemaDiscoveryAction : AnAction() {
  companion object {
    const val ACTION_ID = "GraphQLRestartSchemaDiscovery"
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return

    GraphQLConfigProvider.getInstance(project).invalidate()
    GraphQLGeneratedSourcesManager.getInstance(project).reset()
  }
}
