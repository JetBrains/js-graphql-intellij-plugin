package com.intellij.lang.jsgraphql.ide.config.model

import com.google.common.hash.Hashing
import com.google.gson.Gson
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEnvironmentSnapshot
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.URLUtil
import java.nio.charset.StandardCharsets

private val invalidPathCharsRegex = Regex("[{}$*,]")

data class GraphQLSchemaPointer(
    private val project: Project,
    val dir: VirtualFile,
    val rawData: GraphQLRawSchemaPointer,
    val isLegacy: Boolean,
    val environment: GraphQLEnvironmentSnapshot,
) {
    companion object {
        @JvmStatic
        fun createPathForLocal(pointer: GraphQLSchemaPointer): String? {
            val filePath = pointer.filePath ?: return null

            return FileUtil.toSystemDependentName(
                if (FileUtil.isAbsolute(pointer.filePath)) {
                    pointer.filePath
                } else {
                    FileUtil.join(pointer.dir.path, filePath)
                }
            )
        }

        @JvmStatic
        fun createPathForRemote(pointer: GraphQLSchemaPointer): String? {
            return createFileNameForRemote(pointer)
                ?.let { FileUtil.toSystemDependentName(FileUtil.join(GraphQLRemoteSchemasRegistry.remoteSchemasDirPath, it)) }
        }

        @Suppress("UnstableApiUsage")
        @JvmStatic
        fun createFileNameForRemote(pointer: GraphQLSchemaPointer): String? {
            val url = pointer.url ?: return null
            val headers = try {
                Gson().toJson(pointer.headers)
            } catch (e: Exception) {
                thisLogger().warn(e)
                ""
            }

            val name = Hashing.sha256().newHasher()
                .putString(url, StandardCharsets.UTF_8)
                .putString(headers, StandardCharsets.UTF_8)
                .putString(FileUtil.toSystemIndependentName(pointer.dir.path), StandardCharsets.UTF_8)
                .hash()
                .toString()

            return "$name.graphql"
        }
    }

    val pattern: String =
        expandVariables(rawData.pattern, createExpandContext()) ?: rawData.pattern

    val headers: Map<String, Any?> =
        parseMap(expandVariables(rawData.headers, createExpandContext())) ?: emptyMap()

    val isRemote: Boolean = URLUtil.canContainUrl(pattern)

    val url: String? = pattern.takeIf { isRemote }

    val filePath: String? = pattern.takeUnless { URLUtil.canContainUrl(it) || it.contains(invalidPathCharsRegex) }

    val globPath: String? = pattern.takeUnless { URLUtil.canContainUrl(it) || it.contains('$') }

    val outputPath: String? = if (isRemote) {
        createPathForRemote(this)
    } else if (filePath != null) {
        createPathForLocal(this)
    } else {
        null
    }

    private fun createExpandContext() = GraphQLExpandVariableContext(project, dir, isLegacy, environment)
}
