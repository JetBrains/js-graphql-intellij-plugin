package com.intellij.lang.jsgraphql.ide.introspection.source

import com.google.gson.JsonSyntaxException
import com.intellij.json.JsonFileType
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

private val HIDE_ERROR_KEY = Key.create<Boolean?>("graphql.show.sdl.generation.error")

class GraphQLGeneratedSourcesEditorNotificationProvider : EditorNotificationProvider {

  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
    return Function { fileEditor ->
      if (file.fileType != JsonFileType.INSTANCE || fileEditor.getUserData(HIDE_ERROR_KEY) == true) return@Function null
      val error = GraphQLGeneratedSourcesManager.getInstance(project).getErrorForSource(file) ?: return@Function null

      EditorNotificationPanel(fileEditor, JBUI.CurrentTheme.Notification.Error.BACKGROUND).apply {
        text = if (error is JsonSyntaxException) {
          GraphQLBundle.message("graphql.notification.sdl.generation.syntax.error.text")
        }
        else {
          GraphQLBundle.message("graphql.notification.sdl.generation.error.text")
        }
        icon(GraphQLIcons.FILE)
        createActionLabel(
          GraphQLBundle.message("graphql.notification.sdl.generation.stack.trace.action"), {
          val stackTrace = ExceptionUtil.getThrowableText(error)
          val stackTraceFile = PsiFileFactory.getInstance(project)
            .createFileFromText("sdl-generation-error.txt", PlainTextLanguage.INSTANCE, stackTrace)
          OpenFileDescriptor(project, stackTraceFile.virtualFile).navigate(true)
        }, false
        )
        createActionLabel(GraphQLBundle.message("graphql.notification.sdl.generation.hide.notification.action"), {
          fileEditor.putUserData(HIDE_ERROR_KEY, true)
          EditorNotifications.getInstance(project).updateNotifications(file)
        }, false)
      }
    }
  }
}
