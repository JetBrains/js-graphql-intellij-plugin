package com.intellij.lang.jsgraphql.ide.config.loader

import com.google.gson.Gson
import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.config.*
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor

@Service(Service.Level.PROJECT)
class GraphQLConfigLoader(private val project: Project) {

  companion object {
    private val LOG = logger<GraphQLConfigLoader>()

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLConfigLoader>()
  }

  fun load(file: VirtualFile): Result {
    val raw = try {
      readData(file)
    }
              catch (e: Throwable) {
                LOG.info("Unable to load config: ${file.path}", e)
                return Result(null, GraphQLConfigEvaluationStatus.ERROR, e)
              } ?: return Result(null, GraphQLConfigEvaluationStatus.EMPTY)

    val isLegacy = isLegacyConfig(file)
    val root = parseProjectConfig(raw, isLegacy)
    val projects = raw[GraphQLConfigKeys.PROJECTS]
      ?.asSafely<Map<*, *>>()
      ?.mapNotNull { (key, value) ->
        val name = key as? String
        val data = value as? Map<*, *>

        if (name != null && data != null) {
          name to parseProjectConfig(data, isLegacy)
        }
        else {
          null
        }
      }
      ?.toMap()

    return Result(GraphQLRawConfig(root, projects), GraphQLConfigEvaluationStatus.SUCCESS)
  }

  private fun parseProjectConfig(data: Map<*, *>, isLegacy: Boolean): GraphQLRawProjectConfig {
    if (isLegacy) return parseLegacyProjectConfig(data)

    val schema: List<GraphQLRawSchemaPointer>? = parseSchemas(data[GraphQLConfigKeys.SCHEMA])
    val documents: List<String>? = parseListOrItem(data[GraphQLConfigKeys.DOCUMENTS])
    val extensions: Map<String, Any?>? = parseMap(data[GraphQLConfigKeys.EXTENSIONS])
    val include: List<String>? = parseListOrItem(data[GraphQLConfigKeys.INCLUDE])
    val exclude: List<String>? = parseListOrItem(data[GraphQLConfigKeys.EXCLUDE])

    return GraphQLRawProjectConfig(schema, documents, extensions, include, exclude)
  }

  private fun parseSchemas(data: Any?): List<GraphQLRawSchemaPointer>? {
    return when (data) {
      is String -> listOf(GraphQLRawSchemaPointer(data))

      is List<*> -> data.mapNotNull { value ->
        when (value) {
          is String -> GraphQLRawSchemaPointer(value)
          is Map<*, *> -> parseRemoteSchema(value)
          else -> null
        }
      }

      else -> null
    }
  }

  private fun parseRemoteSchema(data: Map<*, *>): GraphQLRawSchemaPointer? {
    val (key, params) = data.entries.firstOrNull() ?: return null
    val url = key as? String ?: return null
    val paramsMap = params.asSafely<Map<*, *>>()

    val headers = paramsMap
                    ?.get(GraphQLConfigKeys.HEADERS)
                    ?.asSafely<Map<*, *>>()
                    ?.mapNotNull { (k, v) ->
                      val header = k as? String
                      if (header != null) {
                        header to v
                      }
                      else {
                        null
                      }
                    }
                    ?.toMap()
                  ?: emptyMap()

    val introspect = paramsMap?.get(GraphQLConfigKeys.INTROSPECT) as? Boolean

    return GraphQLRawSchemaPointer(url, headers, introspect)
  }

  private inline fun <reified T> parseListOrItem(data: Any?): List<T>? {
    return when (data) {
      is T -> listOf(data)
      is List<*> -> data.filterIsInstance<T>()
      else -> null
    }
  }

  private fun parseLegacyProjectConfig(data: Map<*, *>): GraphQLRawProjectConfig {
    val schemaPath: List<GraphQLRawSchemaPointer>? = parseSchemas(data[GraphQLConfigKeys.LEGACY_SCHEMA_PATH])
    val includes: List<String>? = parseListOrItem(data[GraphQLConfigKeys.LEGACY_INCLUDES])
    val excludes: List<String>? = parseListOrItem(data[GraphQLConfigKeys.LEGACY_EXCLUDES])
    val extensions: Map<String, Any?>? = parseMap(data[GraphQLConfigKeys.LEGACY_EXTENSIONS])

    return GraphQLRawProjectConfig(schemaPath, emptyList(), extensions, includes, excludes)
  }

  private fun readData(file: VirtualFile): Map<*, *>? {
    return when (file.extension) {
      "json" -> readJson(file)
      "yaml", "yml" -> readYml(file)
      "js", "cjs", "ts" -> readJs(file)
      else -> when (file.name) {
        GRAPHQLCONFIG -> readJson(file)
        GRAPHQL_RC -> readContentDependent(file)
        else -> throw IllegalArgumentException("unknown config file format: ${file.path}")
      }
    }
  }

  private fun readContentDependent(file: VirtualFile): Map<*, *>? {
    val text = loadText(file) ?: return null
    val firstChar = text.getOrNull(0)
    return if (firstChar == '[' || firstChar == '{' || firstChar == '/') {
      readJson(text)
    }
    else {
      readYml(text)
    }
  }

  private fun readJson(file: VirtualFile): Map<*, *>? {
    val text = loadText(file) ?: return null
    return readJson(text)
  }

  private fun readJson(text: String): Map<*, *>? = Gson().fromJson(text, Map::class.java)

  private fun readYml(file: VirtualFile): Map<*, *>? {
    val text = loadText(file) ?: return null
    return readYml(text)
  }

  private fun readYml(text: String) = Yaml(SafeConstructor(LoaderOptions())).load(text) as? Map<*, *>

  private fun readJs(file: VirtualFile): Map<*, *>? {
    val loader = GraphQLConfigCustomLoader.forFile(file)
    if (loader == null) {
      val msg = "custom loader not found for ${file.path}"
      LOG.warn(msg)
      throw IllegalArgumentException(msg)
    }
    return loader.load(project, file)
  }

  private fun loadText(file: VirtualFile): String? =
    runReadAction { VfsUtil.loadText(file) }.takeIf { it.isNotBlank() }

  data class Result(val data: GraphQLRawConfig?, val status: GraphQLConfigEvaluationStatus, val error: Throwable? = null)
}

