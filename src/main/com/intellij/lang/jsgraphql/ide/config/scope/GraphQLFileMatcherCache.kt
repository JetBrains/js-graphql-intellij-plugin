package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * It is recommended to use this class instead of a standard Map<VirtualFile, Boolean?>
 * due to the potential for a large number of files in a project.
 */
class GraphQLFileMatcherCache {
  companion object {
    fun newInstance(project: Project, vararg dependencies: Any): CachedValue<GraphQLFileMatcherCache> =
      CachedValuesManager.getManager(project).createCachedValue {
        CachedValueProvider.Result.create(
          GraphQLFileMatcherCache(),
          GraphQLScopeDependency.getInstance(project),
          *dependencies
        )
      }
  }

  private val lock = ReentrantReadWriteLock()
  private val matchingFiles = VfsUtil.createCompactVirtualFileSet() // lock
  private val excludedFiles = VfsUtil.createCompactVirtualFileSet() // lock

  fun match(virtualFile: VirtualFile, matcher: (VirtualFile) -> Boolean): Boolean {
    val status = getMatchResult(virtualFile)
    if (status != Match.UNKNOWN) {
      return status == Match.MATCHING
    }

    return cacheResult(virtualFile, matcher(virtualFile)) == Match.MATCHING
  }

  private fun getMatchResult(virtualFile: VirtualFile): Match {
    return lock.read {
      if (matchingFiles.contains(virtualFile)) {
        Match.MATCHING
      }
      else if (excludedFiles.contains(virtualFile)) {
        Match.EXCLUDED
      }
      else {
        Match.UNKNOWN
      }
    }
  }

  private fun cacheResult(virtualFile: VirtualFile, isMatching: Boolean): Match {
    return lock.write {
      // need to re-check to prevent races
      val concurrentMatch = getMatchResult(virtualFile)

      if (concurrentMatch != Match.UNKNOWN) {
        concurrentMatch
      }
      else if (isMatching) {
        matchingFiles.add(virtualFile)
        Match.MATCHING
      }
      else {
        excludedFiles.add(virtualFile)
        Match.EXCLUDED
      }
    }
  }

  enum class Match {
    UNKNOWN,
    MATCHING,
    EXCLUDED,
  }
}
