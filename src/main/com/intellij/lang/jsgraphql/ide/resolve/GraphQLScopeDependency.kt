package com.intellij.lang.jsgraphql.ide.resolve

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager

@Service(Service.Level.PROJECT)
class GraphQLScopeDependency(project: Project) : ModificationTracker {
  private val projectRootManager = ProjectRootManager.getInstance(project)
  private val modificationTracker = SimpleModificationTracker()

  override fun getModificationCount(): Long =
    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS.modificationCount +
    projectRootManager.modificationCount +
    modificationTracker.modificationCount

  fun update() {
    modificationTracker.incModificationCount()
  }

  companion object {
    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLScopeDependency>()
  }
}
