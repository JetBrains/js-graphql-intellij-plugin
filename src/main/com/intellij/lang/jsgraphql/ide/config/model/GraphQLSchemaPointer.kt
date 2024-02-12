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
  val pattern: String =
    expandVariables(rawData.pattern, expandContext) ?: rawData.pattern

  val headers: Map<String, Any?> =
    parseMap(expandVariables(rawData.headers, expandContext)) ?: emptyMap()

  val isRemote: Boolean = URLUtil.canContainUrl(pattern)

  val url: String? = pattern.takeIf { isRemote }

  val filePath: String? = pattern.takeUnless { URLUtil.canContainUrl(it) || it.contains(invalidPathCharsRegex) }

  val globPath: String? = pattern.takeUnless { URLUtil.canContainUrl(it) || it.contains('$') }

  val outputPath: String? = when {
    isRemote -> createPathForRemote(this)
    filePath != null -> createPathForLocal(this)
    else -> null
  }

  private val expandContext
    get() = GraphQLExpandVariableContext(project, dir, isLegacy, environment)

  companion object {
    @JvmStatic
    fun createPathForLocal(pointer: GraphQLSchemaPointer): String? =
      pointer.filePath?.let { resolvePath(it, pointer.dir) }

    @JvmStatic
    fun createPathForLocal(rawPattern: String, dir: VirtualFile): String? =
      if (!URLUtil.canContainUrl(rawPattern) && !rawPattern.contains(invalidPathCharsRegex)) resolvePath(rawPattern, dir) else null

    private fun resolvePath(path: String, dir: VirtualFile): String =
      FileUtil.toCanonicalPath(if (FileUtil.isAbsolute(path)) path else FileUtil.join(dir.path, path))

    @JvmStatic
    fun createPathForRemote(pointer: GraphQLSchemaPointer): String? {
      return createFileNameForRemote(pointer)
        ?.let { FileUtil.toCanonicalPath(FileUtil.join(GraphQLRemoteSchemasRegistry.remoteSchemasDirPath, it)) }
    }

    @Suppress("UnstableApiUsage")
    @JvmStatic
    fun createFileNameForRemote(pointer: GraphQLSchemaPointer): String? {
      val url = pointer.url ?: return null
      val headers = try {
        Gson().toJson(pointer.headers)
      }
      catch (e: Exception) {
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
}
