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
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * Matcher which uses nashorn-minimatch to achieve same glob semantics as graphql-config.
 */
@Service(Service.Level.PROJECT)
class GraphQLConfigGlobMatcher(private val project: Project) {
    companion object {
        private val LOG = logger<GraphQLConfigGlobMatcher>()

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigGlobMatcher>()
    }

    private val matches: MutableMap<Pair<String, String>, Boolean> = ConcurrentHashMap()

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

        return matches.computeIfAbsent(path to glob) { (path, glob) ->
            try {
                FileSystems.getDefault().getPathMatcher("glob:$glob").matches(Path.of(path))
            } catch (e: Exception) {
                LOG.warn("path=$path, glob=$glob", e)
                false
            }
        }
    }
}
