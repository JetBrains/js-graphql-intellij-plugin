package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService.IntrospectionOutput
import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.lang.jsgraphql.ide.notifications.addShowQueryErrorDetailsAction
import com.intellij.lang.jsgraphql.ide.notifications.notifyAboutPossiblyInvalidIntrospectionSchema
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.readAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

class GraphQLConvertJsonIntrospectionToSdlCommand(
  private val project: Project,
  private val elementPointer: SmartPsiElementPointer<PsiElement>,
) : Runnable {
  companion object {
    private val LOG = logger<GraphQLConvertJsonIntrospectionToSdlCommand>()
  }

  override fun run() {
    GraphQLConvertJsonIntrospectionToSdlCommandScope.get(project).launch {
      val element = readAction { elementPointer.element } ?: run {
        LOG.warn("Failed to find PSI element for introspection JSON file")
        return@launch
      }

      try {
        val (introspectionJson, outputDir, outputFileName) = readAction {
          val introspectionJson = element.containingFile.text
          val jsonFile = element.containingFile.virtualFile
          val outputDir = jsonFile.parent
          val outputFileName = jsonFile.nameWithoutExtension + ".graphql"
          Triple(introspectionJson, outputDir, outputFileName)
        }
        if (outputDir == null) {
          LOG.error("Failed to find output directory for introspection JSON file")
          return@launch
        }
        val schemaAsSDL = GraphQLIntrospectionService.printIntrospectionAsGraphQL(project, introspectionJson)

        GraphQLIntrospectionSchemaWriter.getInstance(project).createOrUpdateIntrospectionFile(
          IntrospectionOutput(schemaAsSDL, GraphQLIntrospectionService.IntrospectionOutputFormat.SDL),
          outputDir,
          outputFileName,
        )
      }
      catch (e: CancellationException) {
        throw e
      }
      catch (e: Exception) {
        val notification = Notification(
          GRAPHQL_NOTIFICATION_GROUP_ID,
          GraphQLBundle.message("graphql.notification.introspection.error.title"),
          GraphQLBundle.message("graphql.notification.introspection.error.body"),
          NotificationType.ERROR
        ).setImportant(true)

        notifyAboutPossiblyInvalidIntrospectionSchema(notification, e)
        addShowQueryErrorDetailsAction(project, notification, e)
        Notifications.Bus.notify(notification)
      }
    }
  }

  @Service(Service.Level.PROJECT)
  private class GraphQLConvertJsonIntrospectionToSdlCommandScope(private val coroutineScope: CoroutineScope) {
    companion object {
      fun get(project: Project) = project.service<GraphQLConvertJsonIntrospectionToSdlCommandScope>().coroutineScope
    }
  }
}