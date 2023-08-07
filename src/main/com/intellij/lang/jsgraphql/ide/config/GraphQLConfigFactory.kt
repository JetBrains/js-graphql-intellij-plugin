/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.OutputStream
import java.util.function.Consumer

@Service(Service.Level.PROJECT)
class GraphQLConfigFactory(private val project: Project) {

  companion object {
    const val PREFERRED_CONFIG = GRAPHQL_CONFIG_YML

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLConfigFactory>()
  }

  @JvmOverloads
  fun createAndOpenConfigFile(
    configBaseDir: VirtualFile,
    openEditor: Boolean,
    outputStreamConsumer: Consumer<OutputStream> = Consumer { outputStream: OutputStream ->
      try {
        javaClass.classLoader.getResourceAsStream("META-INF/$PREFERRED_CONFIG")?.use { inputStream ->
          IOUtils.copy(inputStream, outputStream)
        }
      }
      catch (e: IOException) {
        throw RuntimeException(e)
      }
    },
  ) {
    invokeLater {
      WriteCommandAction.runWriteCommandAction(
        project,
        GraphQLBundle.message("graphql.action.create.config.file.command"),
        null,
        {
          try {
            val configFile = configBaseDir.createChildData(this, PREFERRED_CONFIG)
            configFile.getOutputStream(this).use { stream -> outputStreamConsumer.accept(stream) }
            val psiFile = PsiManager.getInstance(project).findFile(configFile)
            if (psiFile != null) {
              CodeStyleManager.getInstance(project).reformat(psiFile)
            }
            if (openEditor) {
              FileEditorManager.getInstance(project).openFile(configFile, true, true)
            }
            invokeLater {
              ApplicationManager.getApplication().saveAll()
            }
          }
          catch (e: IOException) {
            Notifications.Bus.notify(
              Notification(
                GRAPHQL_NOTIFICATION_GROUP_ID,
                GraphQLBundle.message("graphql.notification.title.unable.to.create", PREFERRED_CONFIG),
                GraphQLBundle.message("graphql.notification.content.unable.to.create.file.in.directory", PREFERRED_CONFIG, configBaseDir.path, e.message),
                NotificationType.ERROR
              )
            )
          }
        }
      )
    }
  }
}
