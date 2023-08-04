package com.intellij.lang.jsgraphql.ide.config.migration

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.icons.GraphQLIcons
import com.intellij.lang.jsgraphql.ide.config.GRAPHQLCONFIG
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import java.util.function.Function
import javax.swing.JComponent

class GraphQLMigrateLegacyConfigEditorNotificationProvider : EditorNotificationProvider {
  override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?> {
    return Function {
      if (file.name != GRAPHQLCONFIG) return@Function null

      EditorNotificationPanel().apply {
        text = GraphQLBundle.message("graphql.notification.migrate.config.text")
        icon(GraphQLIcons.FILE)
        createActionLabel(
          GraphQLBundle.message("graphql.notification.migrate.config.action"),
          GraphQLMigrateLegacyConfigAction.ACTION_ID
        )
      }
    }
  }
}
