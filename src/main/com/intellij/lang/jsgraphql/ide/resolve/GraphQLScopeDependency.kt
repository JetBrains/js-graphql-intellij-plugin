package com.intellij.lang.jsgraphql.ide.resolve

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.CompositeModificationTracker
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager

@Service(Service.Level.PROJECT)
class GraphQLScopeDependency : ModificationTracker {
  companion object {
    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLScopeDependency>()
  }

  private val modificationTracker = CompositeModificationTracker(VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS)

  override fun getModificationCount(): Long = modificationTracker.modificationCount

  fun update() {
    modificationTracker.incModificationCount()
  }
}
