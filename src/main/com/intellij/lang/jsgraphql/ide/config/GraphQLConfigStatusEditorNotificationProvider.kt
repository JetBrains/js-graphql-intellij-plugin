package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import com.intellij.util.ExceptionUtil
import com.intellij.util.ui.JBUI
import java.util.function.Function
import javax.swing.JComponent

private val RELOAD_TIMESTAMP_KEY = Key.create<Long?>("graphql.editor.notification.reload.timestamp")

class GraphQLConfigStatusEditorNotificationProvider : EditorNotificationProvider {
  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
    return Function { fileEditor ->
      if (file.name !in CONFIG_NAMES) return@Function null

      val provider = GraphQLConfigProvider.getInstance(project)
      val state = provider.getConfigEvaluationState(file) ?: return@Function null
      if (state.status != GraphQLConfigEvaluationStatus.ERROR ||
          fileEditor.getUserData(RELOAD_TIMESTAMP_KEY) == provider.modificationCount
      ) {
        return@Function null
      }

      fileEditor.putUserData(RELOAD_TIMESTAMP_KEY, null)
      EditorNotificationPanel(fileEditor, JBUI.CurrentTheme.Notification.Error.BACKGROUND).apply {
        text = GraphQLBundle.message("graphql.config.evaluation.error")
        icon(GraphQLIcons.FILE)
        val cause = state.error?.cause
        if (cause != null) {
          createActionLabel(
            GraphQLBundle.message("graphql.notification.sdl.generation.stack.trace.action"), {
            val stackTrace = ExceptionUtil.getThrowableText(cause)
            val stackTraceFile = PsiFileFactory.getInstance(project)
              .createFileFromText("config-error.txt", PlainTextLanguage.INSTANCE, stackTrace)
            OpenFileDescriptor(project, stackTraceFile.virtualFile).navigate(true)
          }, false
          )
        }
        createActionLabel(GraphQLBundle.message("graphql.config.reload"), {
          fileEditor.putUserData(RELOAD_TIMESTAMP_KEY, provider.modificationCount)
          provider.invalidate(file)
          EditorNotifications.getInstance(project).updateNotifications(file)
        }, false)
      }
    }
  }
}
