package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigGlobMatcher
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class GraphQLProjectConfig(
    private val project: Project,
    val file: VirtualFile,
    val name: String,
    private val rawConfig: GraphQLRawProjectConfig,
    private val parentConfig: GraphQLRawProjectConfig?,
) {

    val schema: List<GraphQLSchemaPointer> = rawConfig.schema ?: parentConfig?.schema ?: emptyList()

    val documents: List<String> = rawConfig.documents ?: parentConfig?.documents ?: emptyList()

    val extensions: Map<String, Any?> = buildMap {
        parentConfig?.extensions?.let { putAll(it) }
        rawConfig.extensions?.let { putAll(it) }
    }

    val include: List<String> = rawConfig.include ?: parentConfig?.include ?: emptyList()

    val exclude: List<String> = rawConfig.exclude ?: parentConfig?.exclude ?: emptyList()

    private val matchingCache =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                MatchingFilesCache(),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
        }

    fun match(context: PsiFile): Boolean {
        return getPhysicalVirtualFile(context)?.let { match(it) } ?: false
    }

    fun match(virtualFile: VirtualFile): Boolean {
        val cache = matchingCache.value
        val status = cache.getMatchResult(virtualFile)
        if (status != MatchResult.UNKNOWN) {
            return status == MatchResult.MATCHING
        }

        return matchImpl(virtualFile).also { cache.cacheResult(virtualFile, it) }
    }

    private fun matchImpl(virtualFile: VirtualFile): Boolean {
        val isSchemaOrDocument = sequenceOf(schema, documents).any { match(virtualFile, it) }
        if (isSchemaOrDocument) {
            return true;
        }

        val isExcluded = if (exclude.isNotEmpty()) match(virtualFile, exclude) else false
        if (isExcluded) {
            return false;
        }

        return if (include.isNotEmpty()) match(virtualFile, include) else false
    }

    private fun match(candidate: VirtualFile, pointer: Any?): Boolean {
        return when (pointer) {
            is List<*> -> pointer.any { match(candidate, it) }

            is String -> {
                val path = VfsUtil.findRelativePath(file, candidate, File.separatorChar)
                    ?.let { FileUtil.toCanonicalPath(it) } ?: return false
                val glob = FileUtil.toCanonicalPath(pointer)
                GraphQLConfigGlobMatcher.getInstance(project).matches(path, glob)
            }

            is GraphQLSchemaPointer -> if (pointer.isRemote) {
                false
            } else {
                match(candidate, pointer.pathOrUrl)
            }

            else -> false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphQLProjectConfig

        if (project != other.project) return false
        if (file != other.file) return false
        if (name != other.name) return false
        if (rawConfig != other.rawConfig) return false
        if (parentConfig != other.parentConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = project.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + rawConfig.hashCode()
        result = 31 * result + (parentConfig?.hashCode() ?: 0)
        return result
    }

    private class MatchingFilesCache {
        private val matchingFiles = VfsUtil.createCompactVirtualFileSet() // lock
        private val excludedFiles = VfsUtil.createCompactVirtualFileSet() // lock
        private val lock = ReentrantReadWriteLock()

        fun getMatchResult(virtualFile: VirtualFile): MatchResult {
            return lock.read {
                if (matchingFiles.contains(virtualFile)) {
                    MatchResult.MATCHING
                } else if (excludedFiles.contains(virtualFile)) {
                    MatchResult.EXCLUDED
                } else {
                    MatchResult.UNKNOWN
                }
            }
        }

        fun cacheResult(virtualFile: VirtualFile, isMatching: Boolean) {
            lock.write {
                if (!matchingFiles.contains(virtualFile) && !excludedFiles.contains(virtualFile)) {
                    if (isMatching) {
                        matchingFiles.add(virtualFile)
                    } else {
                        excludedFiles.add(virtualFile)
                    }
                }
            }
        }
    }

    private enum class MatchResult {
        UNKNOWN,
        MATCHING,
        EXCLUDED,
    }
}
