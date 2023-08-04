package com.intellij.lang.jsgraphql.ide.actions

import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GraphQLRestartSchemaDiscoveryAction : AnAction(
  GraphQLBundle.message("graphql.action.restart.schema.discovery.title"),
  GraphQLBundle.message("graphql.action.restart.schema.discovery.desc"),
  AllIcons.Actions.Restart,
) {
  companion object {
    const val ACTION_ID = "GraphQLRestartSchemaDiscovery"
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return

    GraphQLConfigProvider.getInstance(project).invalidate()
    GraphQLGeneratedSourcesManager.getInstance(project).reset()
  }
}
