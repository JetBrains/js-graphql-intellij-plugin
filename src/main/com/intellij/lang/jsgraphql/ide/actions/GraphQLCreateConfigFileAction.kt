package com.intellij.lang.jsgraphql.ide.actions

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile

class GraphQLCreateConfigFileAction : AnAction() {
  override fun getActionUpdateThread(): ActionUpdateThread {
    return ActionUpdateThread.BGT
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project
    if (project != null) {
      val virtualFile = getActionDirectory(e)
      if (virtualFile != null) {
        GraphQLConfigFactory.getInstance(project).createAndOpenConfigFile(virtualFile, true)
        ApplicationManager.getApplication().saveAll()
      }
    }
  }

  override fun update(e: AnActionEvent) {
    var isAvailable = false
    if (e.project != null) {
      val dataContext = e.dataContext
      val view = LangDataKeys.IDE_VIEW.getData(dataContext)
      if (view != null && view.directories.isNotEmpty()) {
        val module = LangDataKeys.MODULE.getData(dataContext)
        if (module != null) {
          val actionDirectory = getActionDirectory(e)
          if (actionDirectory != null) {
            isAvailable = actionDirectory.findChild(GraphQLConfigFactory.PREFERRED_CONFIG) == null
          }
        }
      }
    }
    val presentation = e.presentation
    presentation.isVisible = isAvailable
    presentation.isEnabled = isAvailable
  }

  private fun getActionDirectory(e: AnActionEvent): VirtualFile? {
    var virtualFile = e.dataContext.getData(LangDataKeys.VIRTUAL_FILE)
    if (virtualFile != null) {
      if (!virtualFile.isDirectory) {
        virtualFile = virtualFile.parent
      }
    }
    return virtualFile
  }
}
