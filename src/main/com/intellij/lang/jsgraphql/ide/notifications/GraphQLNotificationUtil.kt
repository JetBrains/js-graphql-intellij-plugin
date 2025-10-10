@file:JvmName("GraphQLNotificationUtil")

package com.intellij.lang.jsgraphql.ide.notifications

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLConstants
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.fileLogger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.util.ObjectUtils
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Nls
import javax.net.ssl.SSLException

const val GRAPHQL_NOTIFICATION_GROUP_ID: String = GraphQLConstants.GraphQL

fun showInvalidConfigurationNotification(
  message: @Nls String,
  configFile: VirtualFile?,
  project: Project,
) {
  val notification = Notification(
    GRAPHQL_NOTIFICATION_GROUP_ID,
    GraphQLBundle.message("graphql.notification.invalid.config.file"),
    message,
    NotificationType.WARNING
  )
  if (configFile != null) {
    notification.addAction(
      object : NotificationAction(GraphQLBundle.message("graphql.notification.open.file", configFile.name)) {
        override fun actionPerformed(e: AnActionEvent, notification: Notification) {
          FileEditorManager.getInstance(project).openFile(configFile, true)
        }
      })
  }
  Notifications.Bus.notify(notification)
}

fun addRetryQueryAction(notification: Notification, runnable: Runnable) {
  notification.addAction(object : NotificationAction(GraphQLBundle.message("graphql.notification.retry")) {
    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
      notification.expire()
      runnable.run()
    }
  })
}

fun addShowQueryErrorDetailsAction(project: Project, notification: Notification, exception: Exception) {
  notification.addAction(object : NotificationAction(GraphQLBundle.message("graphql.notification.show.query.error.details.action")) {
    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
      val errorText = formatExceptionMessage(exception)
      val file = PsiFileFactory.getInstance(project)
        .createFileFromText("query-error.txt", PlainTextLanguage.INSTANCE, errorText)
      OpenFileDescriptor(project, file.getVirtualFile()).navigate(true)
    }
  })
}

internal fun notifyAboutPossiblyInvalidIntrospectionSchema(notification: Notification, e: Exception) {
  if (e is GraphQLException) {
    notification.setContent(GraphQLBundle.message("graphql.notification.introspection.spec.error.body"))
  }
}

@ApiStatus.ScheduledForRemoval
@Deprecated(message = "use handleGenericRequestError instead")
fun showGraphQLRequestErrorNotification(
  project: Project,
  url: @NlsSafe String,
  error: Exception,
  notificationType: NotificationType,
  @Suppress("unused") action: NotificationAction?,
) {
  handleGenericRequestError(project, url, error, notificationType, null)
}

@JvmOverloads
fun handleGenericRequestError(
  project: Project,
  url: @NlsSafe String,
  error: Exception,
  notificationType: NotificationType,
  runnable: Runnable? = null,
) {
  fileLogger().warn(error)

  val isSSLError = error is SSLException
  val message = if (isSSLError)
    GraphQLBundle.message("graphql.notification.ssl.cert.error.title")
  else
    GraphQLBundle.message("graphql.notification.error.title")

  val notification = Notification(
    GRAPHQL_NOTIFICATION_GROUP_ID,
    message,
    GraphQLBundle.message("graphql.notification.content.request.to.url.failed", url),
    notificationType,
  )
  if (isSSLError) {
    GraphQLIntrospectionService.getInstance(project).createTrustAllHostsAction()?.let { notification.addAction(it) }
  }
  if (runnable != null) {
    addRetryQueryAction(notification, runnable)
  }
  addShowQueryErrorDetailsAction(project, notification, error)
  Notifications.Bus.notify(notification, project)
}

fun handleIntrospectionError(
  project: Project,
  endpoint: GraphQLConfigEndpoint,
  e: Exception,
  content: @Nls String?,
  responseJson: String,
) {
  fileLogger().warn(e)

  val body = content ?: GraphQLBundle.message("graphql.notification.introspection.error.body")

  val notification = Notification(
    GRAPHQL_NOTIFICATION_GROUP_ID,
    GraphQLBundle.message("graphql.notification.introspection.error.title"),
    body,
    NotificationType.ERROR
  ).setImportant(true)

  addRetryQueryAction(notification) {
    GraphQLIntrospectionService.getInstance(project).performIntrospectionQuery(endpoint)
  }
  notifyAboutPossiblyInvalidIntrospectionSchema(notification, e)
  addShowQueryErrorDetailsAction(project, notification, e)
  Notifications.Bus.notify(notification, project)

  GraphQLUIProjectService.getInstance(project).showQueryResult(responseJson)
}

@NlsSafe
fun formatExceptionMessage(throwable: Throwable): String {
  return StringUtil.decapitalize(ObjectUtils.coalesce(throwable.message, ""))
}
