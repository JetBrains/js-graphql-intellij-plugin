/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.actions

import com.intellij.icons.AllIcons
import com.intellij.ide.util.DefaultPsiElementCellRenderer
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.notifications.GRAPHQL_NOTIFICATION_GROUP_ID
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.CommonProcessors
import java.awt.BorderLayout
import javax.swing.JComponent

class GraphQLOpenConfigAction : AnAction(
  GraphQLBundle.messagePointer("graphql.action.open.config.file.title"),
  AllIcons.General.Settings
) {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.getData(CommonDataKeys.PROJECT)
    val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
    val virtualFile = getPhysicalVirtualFile(psiFile)
    if (project == null || virtualFile == null) {
      return
    }
    val provider = GraphQLConfigProvider.getInstance(project)
    val config = provider.resolveProjectConfig(psiFile)
    // look for the closest one as a fallback for cases when a file is excluded
    // and not matched by the `resolveConfig` call
    val configFile = config?.file ?: provider.findConfig(psiFile)?.config?.file
    if (configFile != null) {
      val fileEditorManager = FileEditorManager.getInstance(project)
      fileEditorManager.openFile(configFile, true, true)
    }
    else if (config != null) {
      // TODO: create and show in-memory config
    }
    else {
      // no config associated, ask to create one
      val notification = Notification(
        GRAPHQL_NOTIFICATION_GROUP_ID,
        GraphQLBundle.message("graphql.notification.config.not.found.title"),
        GraphQLBundle.message("graphql.notification.config.not.found.body"),
        NotificationType.INFORMATION
      )
      notification.addAction(
        NotificationAction.createSimpleExpiring(GraphQLBundle.message("graphql.notification.config.not.found.create.action")) {
          createConfig(project, psiFile)
        }
      )
      Notifications.Bus.notify(notification, project)
    }
  }

  private fun createConfig(project: Project, psiFile: PsiFile) {
    val virtualFile = getPhysicalVirtualFile(psiFile) ?: return
    val configDirectoryCandidates = getParentDirsUpToContentRoots(project, virtualFile)
    val configFactory = GraphQLConfigFactory.getInstance(project)
    if (configDirectoryCandidates.size == 1) {
      configFactory.createAndOpenConfigFile(configDirectoryCandidates.first(), true)
    }
    else {
      val dialog = GraphQLConfigDirectoryDialog(project, configDirectoryCandidates)
      if (dialog.showAndGet()) {
        dialog.selectedDirectory?.let { configFactory.createAndOpenConfigFile(it, true) }
      }
    }
  }

  internal class GraphQLConfigDirectoryDialog(project: Project, candidates: Collection<VirtualFile>) :
    DialogWrapper(project) {

    private val psiDirectoryFactory = PsiDirectoryFactory.getInstance(project)

    private val configDirectoryCandidates: List<PsiDirectory> = candidates.map { psiDirectoryFactory.createDirectory(it) }

    private val comboBox = ComboBox(CollectionComboBoxModel(configDirectoryCandidates)).apply {
      renderer = DefaultPsiElementCellRenderer()
      setMinimumAndPreferredWidth(450)
      if (itemCount > 0) {
        selectedIndex = 0
      }
    }

    init {
      title = GraphQLBundle.message("graphql.dialog.title.select.graphql.configuration.folder")
      init()
      comboBox.requestFocus()
    }

    override fun getPreferredFocusedComponent(): JComponent? {
      return comboBox
    }

    override fun createCenterPanel(): JComponent? {
      val panel = NonOpaquePanel()
      panel.add(comboBox, BorderLayout.NORTH)
      return panel
    }

    val selectedDirectory: VirtualFile?
      get() {
        val selectedItem = comboBox.selectedItem as? PsiDirectory
        return selectedItem?.virtualFile
      }
  }

  private fun getParentDirsUpToContentRoots(
    project: Project,
    virtualFile: VirtualFile,
  ): Collection<VirtualFile> {
    return if (GraphQLFileType.isGraphQLScratchFile(virtualFile)) {
      project.guessProjectDir()?.let { listOf(it) } ?: emptyList()
    }
    else {
      val directoriesProcessor =
        CommonProcessors.CollectProcessor<VirtualFile>()
      GraphQLResolveUtil.processDirectoriesUpToContentRoot(project, virtualFile, directoriesProcessor)
      directoriesProcessor.results
    }
  }
}
