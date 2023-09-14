package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.getPathMatcher
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.io.File
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.util.concurrent.ConcurrentHashMap


@Service(Service.Level.PROJECT)
class GraphQLConfigGlobMatcher(project: Project) {
  companion object {
    private val LOG = logger<GraphQLConfigGlobMatcher>()

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLConfigGlobMatcher>()
  }

  private val matchResults: CachedValue<MutableMap<Pair<String, String>, Boolean>> =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(
        ConcurrentHashMap(),
        VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
      )
    }

  private val matchers: MutableMap<String, PathMatcher> = ConcurrentHashMap()

  fun matches(file: VirtualFile, pattern: String, context: VirtualFile): Boolean {
    val path = if (isAbsolutePattern(pattern)) {
      file.path
    }
    else {
      VfsUtil.findRelativePath(context, file, File.separatorChar)
    }?.let { FileUtil.toCanonicalPath(it) }

    val glob = FileUtil.toCanonicalPath(pattern)
    return matches(path, glob).also {
      LOG.trace { "path=${file.path}, pattern=${pattern}, context=${context.path}, result=${it}" }
    }
  }

  private fun isAbsolutePattern(string: String): Boolean {
    if (ApplicationManager.getApplication().isUnitTestMode) {
      return string.startsWith("/")
    }

    return FileUtil.isAbsolute(string)
  }

  private fun matches(path: String?, glob: String?): Boolean {
    if (path.isNullOrBlank() || glob.isNullOrBlank()) {
      return false
    }

    return matchResults.value.computeIfAbsent(path to glob) { (path, glob) ->
      try {
        getOrCreateMatcher(glob).matches(Path.of(path))
      }
      catch (e: Exception) {
        LOG.warn("path=$path, glob=$glob", e)
        false
      }
    }
  }

  private fun getOrCreateMatcher(glob: String): PathMatcher {
    val cached = matchers[glob]
    if (cached != null) {
      return cached
    }

    val matcher = getPathMatcher(glob)
    return matchers.putIfAbsent(glob, matcher) ?: matcher
  }
}
