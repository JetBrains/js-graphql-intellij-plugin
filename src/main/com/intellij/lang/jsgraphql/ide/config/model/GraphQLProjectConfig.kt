package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.getPhysicalVirtualFile
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigGlobMatcher
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigScope
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.asSafely
import java.io.File
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

data class GraphQLProjectConfig(
    private val project: Project,
    val dir: VirtualFile,
    val file: VirtualFile?,
    val name: String,
    private val rawConfig: GraphQLRawProjectConfig,
    private val parentConfig: GraphQLRawProjectConfig?,
) {

    val schema: List<GraphQLSchemaPointer> = (rawConfig.schema ?: parentConfig?.schema ?: emptyList()).map {
        GraphQLSchemaPointer(project, dir, it, isLegacy, false)
    }

    val documents: List<String> = rawConfig.documents ?: parentConfig?.documents ?: emptyList()

    val extensions: Map<String, Any?> = buildMap {
        parentConfig?.extensions?.let { putAll(it) }
        rawConfig.extensions?.let { putAll(it) }
    }

    val include: List<String> = rawConfig.include ?: parentConfig?.include ?: emptyList()

    val exclude: List<String> = rawConfig.exclude ?: parentConfig?.exclude ?: emptyList()

    val scope = GraphQLConfigScope(GlobalSearchScope.projectScope(project), this)

    private val endpointsLazy: Lazy<List<GraphQLConfigEndpoint>> = lazy { buildEndpoints() }

    val endpoints = endpointsLazy.value

    val isDefault = name == GraphQLConfig.DEFAULT_PROJECT

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
                val path = VfsUtil.findRelativePath(dir, candidate, File.separatorChar)
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

    val isLegacy
        get() = isLegacyConfig(file)

    @Suppress("UNCHECKED_CAST")
    private fun buildEndpoints(): List<GraphQLConfigEndpoint> {
        val endpointsMap =
            extensions[GraphQLConfigKeys.EXTENSION_ENDPOINTS] as? Map<String, Any?> ?: return emptyList()

        return endpointsMap.mapNotNull { (endpointName: String, value: Any?) ->
            when (value) {
                is String -> {
                    GraphQLRawEndpoint(
                        endpointName,
                        value as String?,
                        false,
                        emptyMap(),
                    )
                }

                is Map<*, *> -> {
                    val endpointObject = value as Map<String, Any?>
                    val url = endpointObject["url"]
                    if (url is String) {
                        GraphQLRawEndpoint(
                            endpointName,
                            url,
                            endpointObject["introspect"] as Boolean? ?: false,
                            endpointObject["headers"].asSafely<Map<String, Any?>>() ?: emptyMap()
                        )
                    } else {
                        null
                    }
                }

                else -> null
            }
        }.map {
            GraphQLConfigEndpoint(
                project,
                it,
                dir,
                GraphQLConfigPointer(file, name),
                isLegacy,
                false
            )
        }
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
