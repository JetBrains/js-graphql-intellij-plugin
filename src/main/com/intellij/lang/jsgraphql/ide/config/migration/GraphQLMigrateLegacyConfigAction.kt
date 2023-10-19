package com.intellij.lang.jsgraphql.ide.config.migration

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.GRAPHQLCONFIG
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigEvaluationStatus
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigLoader
import com.intellij.lang.jsgraphql.ide.config.serialization.GraphQLConfigPrinter
import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.lang.jsgraphql.ide.notifications.formatExceptionMessage
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import java.io.IOException

class GraphQLMigrateLegacyConfigAction : AnAction() {
  companion object {
    const val ACTION_ID = "GraphQLMigrateLegacyConfig"
  }

  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isVisible = e.getData(CommonDataKeys.VIRTUAL_FILE)?.name == GRAPHQLCONFIG
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val sourceFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
    if (sourceFile.name != GRAPHQLCONFIG) return
    val dir = sourceFile.parent.takeIf { it.isValid && it.isDirectory } ?: return

    val result = GraphQLConfigLoader.getInstance(project).load(sourceFile)
    if (result.status == GraphQLConfigEvaluationStatus.ERROR) {
      Notifications.Bus.notify(
        Notification(
          GRAPHQL_NOTIFICATION_GROUP_ID,
          GraphQLBundle.message("graphql.notification.configuration.error"),
          GraphQLBundle.message("graphql.notification.invalid.config.file"),
          NotificationType.ERROR,
        )
      )
      return
    }

    val source = result.data?.let { GraphQLConfigPrinter.toYml(it) }.orEmpty()
    val text = source.replace(Regex("\\$\\{env:([ \\w]+)}")) { match ->
      match.groupValues[1].trim().let { "\${$it}" }
    }

    WriteCommandAction.runWriteCommandAction(project, GraphQLBundle.message("action.GraphQLMigrateLegacyConfig.text"), null, {
      val newFile = try {
        val created = dir.createChildData(this, GraphQLConfigFactory.PREFERRED_CONFIG)
        VfsUtil.saveText(created, text)
        created.refresh(false, false)
        created
      }
      catch (e: IOException) {
        Notifications.Bus.notify(
          Notification(
            GRAPHQL_NOTIFICATION_GROUP_ID,
            GraphQLBundle.message("graphql.notification.error.title"),
            GraphQLBundle.message(
              "graphql.notification.unable.to.create.file",
              GraphQLConfigFactory.PREFERRED_CONFIG,
              dir.path,
              formatExceptionMessage(e)
            ),
            NotificationType.ERROR,
          )
        )
        return@runWriteCommandAction
      }

      try {
        sourceFile.delete(this)
      }
      catch (e: IOException) {
        Notifications.Bus.notify(
          Notification(
            GRAPHQL_NOTIFICATION_GROUP_ID,
            GraphQLBundle.message("graphql.notification.error.title"),
            GraphQLBundle.message(
              "graphql.notification.unable.to.delete.file",
              sourceFile.name,
              dir.path,
              formatExceptionMessage(e)
            ),
            NotificationType.ERROR,
          )
        )
      }

      val fileDocumentManager = FileDocumentManager.getInstance()
      val psiDocumentManager = PsiDocumentManager.getInstance(project)
      val document = fileDocumentManager.getDocument(newFile) ?: return@runWriteCommandAction
      val psiFile = psiDocumentManager.getPsiFile(document) ?: return@runWriteCommandAction
      psiDocumentManager.commitDocument(document)
      CodeStyleManager.getInstance(project).reformat(psiFile)

      FileEditorManager.getInstance(project).openFile(newFile, true)
    })
  }
}
