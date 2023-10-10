package com.intellij.lang.jsgraphql.ide.introspection.remote

import com.intellij.lang.jsgraphql.GRAPHQL_CACHE_DIR_NAME
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.GlobalSearchScopes
import java.util.concurrent.ConcurrentHashMap


@Service(Service.Level.PROJECT)
class GraphQLRemoteSchemasRegistry(private val project: Project) {
  companion object {
    private const val GRAPHQL_REMOTE_DIR = "remote"

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLRemoteSchemasRegistry>()

    @JvmStatic
    val remoteSchemasDirPath: String
      get() = FileUtil.join(PathManager.getConfigPath(), GRAPHQL_CACHE_DIR_NAME, GRAPHQL_REMOTE_DIR)

    private fun isInRemoteSchemasRoot(file: VirtualFile?) =
      file != null &&
      file.fileType == GraphQLFileType.INSTANCE &&
      file.parent?.name == GRAPHQL_REMOTE_DIR &&
      FileUtil.isAncestor(remoteSchemasDirPath, file.path, true)
  }

  private val associations = ConcurrentHashMap<String, String>()

  fun associate(filePath: String, configPath: String) {
    associations[FileUtil.toSystemIndependentName(filePath)] = FileUtil.toSystemIndependentName(configPath)
  }

  private fun getSourcePath(file: VirtualFile?): String? {
    return file?.let { associations[it.path] }
  }

  fun getSourcePath(filePath: String): String? {
    return associations[FileUtil.toSystemIndependentName(filePath)]
  }

  fun getSourceFile(file: VirtualFile?): VirtualFile? {
    return file
      ?.let { getSourcePath(it) }
      ?.let { runReadAction { LocalFileSystem.getInstance().findFileByPath(it) } }
  }

  fun isRemoteSchemaFile(virtualFile: VirtualFile?): Boolean {
    if (getSourcePath(virtualFile) != null) {
      return true
    }
    return isInRemoteSchemasRoot(virtualFile)
  }

  fun createRemoteIntrospectionScope(): GlobalSearchScope {
    val dir = LocalFileSystem.getInstance().findFileByPath(remoteSchemasDirPath)
              ?: return GlobalSearchScope.EMPTY_SCOPE
    return GlobalSearchScopes.directoryScope(project, dir, false)
  }
}
