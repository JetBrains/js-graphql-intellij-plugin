package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.ide.config.CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.openapi.actionSystem.*

class GraphQLEditEnvironmentVariablesAction : AnAction(
    GraphQLBundle.messagePointer("graphql.action.edit.environment.variables.title"),
    AllIcons.Actions.Properties,
) {
    companion object {
        const val ACTION_ID = "GraphQLEditEnvironmentVariables"
    }

    override fun update(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val provider = GraphQLConfigProvider.getInstance(project)
        val inToolbar = e.place == ActionPlaces.EDITOR_TOOLBAR

        val isEnabled = if (virtualFile.name in CONFIG_NAMES) {
            true
        } else if (GraphQLFileType.isGraphQLFile(project, virtualFile) && inToolbar) {
            provider.hasConfigurationFiles
        } else {
            false
        }

        val title = if (inToolbar) {
            GraphQLBundle.message("graphql.action.edit.environment.variables.toolbar.title")
        } else {
            GraphQLBundle.message("graphql.action.edit.environment.variables.title")
        }

        e.presentation.text = title
        e.presentation.isEnabledAndVisible = isEnabled
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val provider = GraphQLConfigProvider.getInstance(project)

        val environment = if (virtualFile.name in CONFIG_NAMES) {
            provider.getForConfigFile(virtualFile)?.environment
        } else {
            provider.resolveConfig(virtualFile)?.parentConfig?.environment
        } ?: return

        GraphQLEnvironmentVariablesDialog(project, environment, virtualFile, false).show()
    }
}
