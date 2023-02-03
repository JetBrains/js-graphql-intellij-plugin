package com.intellij.lang.jsgraphql.ide.config.loader

import com.google.gson.Gson
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.notifications.showParseErrorNotification
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.asSafely
import org.yaml.snakeyaml.Yaml

@Service
class GraphQLConfigLoader(private val project: Project) {

    companion object {
        private val LOG = logger<GraphQLConfigLoader>()

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLConfigLoader>()
    }

    fun load(file: VirtualFile): GraphQLRawConfig? {
        val raw = readData(file) ?: return null
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

        return GraphQLRawConfig(root, projects)
    }

    private fun parseProjectConfig(data: Map<*, *>, isLegacy: Boolean): GraphQLRawProjectConfig {
        if (isLegacy) return parseLegacyProjectConfig(data)

        val schema: List<GraphQLSchemaPointer>? = parseSchemas(data[GraphQLConfigKeys.SCHEMA])
        val documents: List<String>? = parseListOrItem(data[GraphQLConfigKeys.DOCUMENTS])
        val extensions: Map<String, Any?>? = parseExtensions(data[GraphQLConfigKeys.EXTENSIONS])
        val include: List<String>? = parseListOrItem(data[GraphQLConfigKeys.INCLUDE])
        val exclude: List<String>? = parseListOrItem(data[GraphQLConfigKeys.EXCLUDE])

        return GraphQLRawProjectConfig(schema, documents, extensions, include, exclude)
    }

    private fun parseSchemas(data: Any?): List<GraphQLSchemaPointer>? {
        return when (data) {
            is String -> listOf(GraphQLSchemaPointer(data))

            is List<*> -> data.mapNotNull { value ->
                when (value) {
                    is String -> GraphQLSchemaPointer(value)
                    is Map<*, *> -> parseRemoteSchema(value)
                    else -> null
                }
            }

            else -> null
        }
    }

    private fun parseRemoteSchema(data: Map<*, *>): GraphQLSchemaPointer? {
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

        return GraphQLSchemaPointer(url, headers)
    }

    private fun parseExtensions(data: Any?): Map<String, Any?>? {
        if (data is Map<*, *>) {
            return data.mapNotNull { (key, value) ->
                val name = key as? String ?: return@mapNotNull null
                name to value
            }.toMap()
        }

        return null
    }

    private inline fun <reified T> parseListOrItem(data: Any?): List<T>? {
        return when (data) {
            is T -> listOf(data)
            is List<*> -> data.filterIsInstance<T>()
            else -> null
        }
    }

    private fun parseLegacyProjectConfig(data: Map<*, *>): GraphQLRawProjectConfig {
        val schemaPath: List<GraphQLSchemaPointer>? = parseSchemas(data[GraphQLConfigKeys.LEGACY_SCHEMA_PATH])
        val includes: List<String>? = parseListOrItem(data[GraphQLConfigKeys.LEGACY_INCLUDES])
        val excludes: List<String>? = parseListOrItem(data[GraphQLConfigKeys.LEGACY_EXCLUDES])
        val extensions: Map<String, Any?>? = parseExtensions(data[GraphQLConfigKeys.LEGACY_EXTENSIONS])

        return GraphQLRawProjectConfig(schemaPath, emptyList(), extensions, includes, excludes)
    }

    private fun readData(file: VirtualFile): Map<*, *>? {
        return try {
            when (file.extension) {
                "json" -> readJson(file)
                "yaml", "yml" -> readYml(file)
                else -> if (isLegacyConfig(file)) return readJson(file) else return readYml(file)
            }
        } catch (t: Throwable) {
            showParseErrorNotification(project, file, t)
            LOG.warn(t)
            return null
        }
    }

    private fun readJson(file: VirtualFile): Map<*, *>? {
        val text = runReadAction { VfsUtil.loadText(file) }
        return Gson().fromJson(text, Map::class.java)
    }

    private fun readYml(file: VirtualFile): Map<*, *>? {
        val text = runReadAction { VfsUtil.loadText(file) }
        return Yaml().load(text) as? Map<*, *>
    }
}
