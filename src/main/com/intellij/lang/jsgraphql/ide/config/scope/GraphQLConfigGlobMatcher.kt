/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import java.io.File
import java.nio.file.FileSystems
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
    private val groupRegex = Regex("[{}]")

    fun matches(file: VirtualFile, pattern: String, context: VirtualFile): Boolean {
        val path = VfsUtil.findRelativePath(context, file, File.separatorChar)
            ?.let { FileUtil.toCanonicalPath(it) }
        val glob = FileUtil.toCanonicalPath(pattern)
        return matches(path, glob).also {
            LOG.trace { "path=${file.path}, pattern=${pattern}, context=${context.path}, result=${it}" }
        }
    }

    private fun matches(path: String?, glob: String?): Boolean {
        if (path.isNullOrBlank() || glob.isNullOrBlank()) {
            return false
        }

        return matchResults.value.computeIfAbsent(path to glob) { (path, glob) ->
            try {
                getOrCreateMatcher(glob).matches(Path.of(path))
            } catch (e: Exception) {
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

        val expandedGlobs = expandGlob(glob)
        val patterns = buildPatterns(expandedGlobs)
        val matcher = buildMatcher(patterns)
        return matchers.putIfAbsent(glob, matcher) ?: matcher
    }

    private fun expandGlob(glob: String): Collection<String> {
        return buildSet {
            add(glob)
            add(glob.replace("**/", ""))
        }.filter { it.isNotBlank() }
    }

    private fun buildPatterns(patterns: Collection<String>): Collection<String> {
        if (patterns.size <= 1) {
            return patterns
        }

        val result = mutableListOf<String>()
        val union = mutableListOf<String>()

        patterns.forEach {
            // nested groups are not allowed, so we need a separate matcher,
            // e.g. `glob:{file.{graphql,js,css},**/file.{graphql,js,css}}` is invalid
            if (it.contains(groupRegex)) {
                result.add(it)
            } else {
                union.add(it)
            }
        }

        if (union.size > 1) {
            result.add(union.joinToString(separator = ",", prefix = "{", postfix = "}"))
        } else if (union.size == 1) {
            result.add(union.first())
        }

        return result
    }

    private fun buildMatcher(patterns: Collection<String>): PathMatcher {
        val matchers = patterns.mapNotNull {
            try {
                FileSystems.getDefault().getPathMatcher("glob:$it")
            } catch (e: Exception) {
                LOG.warn("buildMatcher: pattern=$it", e)
                null
            }
        }

        return PathMatcher { path: Path -> matchers.any { it.matches(path) } }
    }
}
