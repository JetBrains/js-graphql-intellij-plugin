package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.ide.config.CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.openapi.actionSystem.*

class GraphQLEditEnvironmentVariablesAction : AnAction() {
  companion object {
    const val ACTION_ID = "GraphQLEditEnvironmentVariables"
  }

  override fun update(e: AnActionEvent) {
    val project = e.project ?: return
    val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val provider = GraphQLConfigProvider.getInstance(project)
    val inToolbar = e.place == ActionPlaces.EDITOR_TOOLBAR

    val environment = if (virtualFile.name in CONFIG_NAMES) {
      provider.getForConfigFile(virtualFile)?.environment
    }
    else if (GraphQLFileType.isGraphQLFile(virtualFile) && inToolbar) {
      provider.resolveProjectConfig(virtualFile)?.rootConfig?.environment
    }
    else {
      null
    }

    val isEnabled = environment?.hasVariables ?: false
    val isVisible = environment != null

    val title = if (inToolbar) {
      GraphQLBundle.message("graphql.action.edit.environment.variables.toolbar.title")
    }
    else {
      GraphQLBundle.message("action.GraphQLEditEnvironmentVariables.text")
    }

    e.presentation.text = title
    e.presentation.isEnabled = isEnabled
    e.presentation.isVisible = isVisible
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    val provider = GraphQLConfigProvider.getInstance(project)
    val config = when (virtualFile.name) {
                   in CONFIG_NAMES -> provider.getForConfigFile(virtualFile)
                   else -> provider.resolveProjectConfig(virtualFile)?.rootConfig
                 } ?: return

    GraphQLEnvironmentVariablesDialog(project, config.environment, config.dir, false).show()
  }
}
