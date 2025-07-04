package com.intellij.lang.jsgraphql.ide.config

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.AsyncFileListener
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent

internal class GraphQLConfigFileListener(private val project: Project) : AsyncFileListener {
  override fun prepareChange(events: List<VFileEvent>): AsyncFileListener.ChangeApplier? {
    var configurationsChanged = false
    val watchedDirs = collectWatchedDirectories()

    for (event in events) {
      ProgressManager.checkCanceled()
      if (configurationsChanged) break

      if (event is VFileCreateEvent) {
        if (event.childName in CONFIG_NAMES) {
          configurationsChanged = true
        }
        continue
      }

      val file = event.file ?: continue
      if (file.isDirectory) {
        if (file in watchedDirs || watchedDirs.any { VfsUtil.isAncestor(file, it, true) }) {
          configurationsChanged = true
        }
      }
      else {
        if (event is VFilePropertyChangeEvent) {
          if (VirtualFile.PROP_NAME == event.propertyName) {
            if (event.newValue is String && event.newValue in CONFIG_NAMES ||
                event.oldValue is String && event.oldValue in CONFIG_NAMES
            ) {
              configurationsChanged = true
            }
          }
        }
        else {
          if (file.name in CONFIG_NAMES) {
            configurationsChanged = true
          }
        }
      }
    }

    return if (configurationsChanged) object : AsyncFileListener.ChangeApplier {
      override fun afterVfsChange() {
        GraphQLConfigProvider.getInstance(project).scheduleConfigurationReload()
      }
    }
    else null
  }

  private fun collectWatchedDirectories() = GraphQLConfigProvider.getInstance(project)
    .getAllConfigs(false)
    .asSequence()
    .map { it.dir }
    .filter { it.isDirectory }
    .toSet()
}