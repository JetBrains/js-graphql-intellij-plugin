package com.intellij.lang.jsgraphql.ide.resolve.scope

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope

class GraphQLModuleLibrariesScope private constructor(project: Project, baseScope: GlobalSearchScope)
  : DelegatingGlobalSearchScope(baseScope) {

  private val fileIndex = ProjectRootManager.getInstance(project).fileIndex

  override fun contains(file: VirtualFile): Boolean =
    super.contains(file) && fileIndex.getOrderEntriesForFile(file).any { it is LibraryOrderEntry }

  companion object {
    internal const val REGISTRY_KEY = "graphql.config.scope.module.libraries"
    internal val isEnabled get() = Registry.`is`(REGISTRY_KEY, true)

    @JvmStatic
    fun create(project: Project, file: VirtualFile?): GlobalSearchScope {
      val librariesScope = ProjectScope.getLibrariesScope(project)
      val module = file?.let { ProjectRootManager.getInstance(project).fileIndex.getModuleForFile(it) }
      val baseScope = module?.getModuleRuntimeScope(false)?.intersectWith(librariesScope) ?: librariesScope
      return GraphQLModuleLibrariesScope(project, baseScope)
    }
  }
}