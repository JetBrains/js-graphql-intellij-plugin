package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigGlobMatcher
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.containers.ContainerUtil
import java.io.File
import java.util.concurrent.ConcurrentMap

class GraphQLProjectConfig(
    private val project: Project,
    val file: VirtualFile,
    val name: String,
    private val rawConfig: GraphQLRawProjectConfig,
    private val parentConfig: GraphQLRawProjectConfig?,
) {
    companion object {
        private val MATCHES_CACHE_KEY =
            Key.create<CachedValue<ConcurrentMap<GraphQLProjectConfig, Boolean>>>("graphql.project.config.matches")
    }

    val schema: List<GraphQLSchemaPointer> = rawConfig.schema ?: parentConfig?.schema ?: emptyList()

    val documents: List<String> = rawConfig.documents ?: parentConfig?.documents ?: emptyList()

    val extensions: Map<String, Any?> = buildMap {
        parentConfig?.extensions?.let { putAll(it) }
        rawConfig.extensions?.let { putAll(it) }
    }

    val include: List<String> = rawConfig.include ?: parentConfig?.include ?: emptyList()

    val exclude: List<String> = rawConfig.exclude ?: parentConfig?.exclude ?: emptyList()

    fun match(context: PsiFile): Boolean {
        val cache = getMatchesCache(context)
        val cachedMatch = cache[this]
        if (cachedMatch != null) {
            return cachedMatch
        }

        val result = matchImpl(context)
        return cache.putIfAbsent(this, result) ?: result
    }

    private fun matchImpl(context: PsiFile): Boolean {
        // TODO: cover more cases for light files, injections and scratches
        val virtualFile = PsiUtilCore.getVirtualFile(context) ?: return false

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

    private fun getMatchesCache(file: PsiFile): ConcurrentMap<GraphQLProjectConfig, Boolean> {
        return CachedValuesManager.getCachedValue(file, MATCHES_CACHE_KEY) {
            CachedValueProvider.Result.create(
                ContainerUtil.createConcurrentWeakMap(),
                file,
                GraphQLConfigProvider.getInstance(project),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
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
}
