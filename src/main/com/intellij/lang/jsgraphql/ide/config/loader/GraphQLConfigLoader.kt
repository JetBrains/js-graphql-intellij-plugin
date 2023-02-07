package com.intellij.lang.jsgraphql.ide.config.loader

import com.google.gson.Gson
import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import org.yaml.snakeyaml.Yaml

@Service
class GraphQLConfigLoader {

    companion object {
        private val LOG = logger<GraphQLConfigLoader>()

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigLoader>()
    }

    fun load(file: VirtualFile): Result {
        val raw = try {
            readData(file)
        } catch (e: Throwable) {
            LOG.info("Unable to parse config: ${file.path}", e)
            return Result(null, Status.ERROR)
        } ?: return Result(null, Status.EMPTY)

        val isLegacy = isLegacyConfig(file)
        val root = parseProjectConfig(raw, isLegacy)
        val projects = raw[GraphQLConfigKeys.PROJECTS]
            ?.asSafely<Map<*, *>>()
            ?.mapNotNull { (key, value) ->
                val name = key as? String
                val data = value as? Map<*, *>

                if (name != null && data != null) {
                    name to parseProjectConfig(data, isLegacy)
                } else {
                    null
                }
            }
            ?.toMap()
            ?: emptyMap()

        return Result(GraphQLRawConfig(root, projects), Status.SUCCESS)
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
        val headers = params
            .asSafely<Map<*, *>>()
            ?.get(GraphQLConfigKeys.SCHEMA_HEADERS)
            ?.asSafely<Map<*, *>>()
            ?.mapNotNull { (k, v) ->
                val header = k as? String
                val value = v as? String
                if (header != null && value != null) {
                    header to value
                } else {
                    null
                }
            }
            ?.toMap()
            ?: emptyMap()

        return GraphQLRawSchemaPointer(url, headers)
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
            else -> if (isLegacyConfig(file)) return readJson(file) else return readContentDependent(file)
        }
    }

    private fun readContentDependent(file: VirtualFile): Map<*, *>? {
        val text = loadText(file) ?: return null
        val firstChar = text.getOrNull(0)
        return if (firstChar == '[' || firstChar == '{' || firstChar == '/') {
            readJson(text)
        } else {
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

    private fun readYml(text: String) = Yaml().load(text) as? Map<*, *>

    private fun loadText(file: VirtualFile): String? =
        runReadAction { VfsUtil.loadText(file) }.takeIf { it.isNotBlank() }

    data class Result(val data: GraphQLRawConfig?, val status: Status)

    enum class Status {
        SUCCESS,
        ERROR,
        EMPTY,
    }
}
