package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * It is recommended to use this class instead of a standard Map<VirtualFile, Boolean?>
 * due to the potential for a large number of files in a project.
 */
class GraphQLFileMatcherCache {
    private val matchingFiles = VfsUtil.createCompactVirtualFileSet() // lock
    private val excludedFiles = VfsUtil.createCompactVirtualFileSet() // lock
    private val lock = ReentrantReadWriteLock()

    fun getMatchResult(virtualFile: VirtualFile): Match {
        return lock.read {
            if (matchingFiles.contains(virtualFile)) {
                Match.MATCHING
            } else if (excludedFiles.contains(virtualFile)) {
                Match.EXCLUDED
            } else {
                Match.UNKNOWN
            }
        }
    }

    fun cacheResult(virtualFile: VirtualFile, isMatching: Boolean): Match {
        return lock.write {
            // need to re-check to prevent races
            val concurrentMatch = getMatchResult(virtualFile)

            if (concurrentMatch != Match.UNKNOWN) {
                concurrentMatch
            } else if (isMatching) {
                matchingFiles.add(virtualFile)
                Match.MATCHING
            } else {
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
