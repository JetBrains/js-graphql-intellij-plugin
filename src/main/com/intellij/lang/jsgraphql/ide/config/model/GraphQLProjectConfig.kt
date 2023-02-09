package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.getPhysicalVirtualFile
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigGlobMatcher
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigSchemaScope
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigScope
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLFileMatcherCache
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope

data class GraphQLProjectConfig(
    private val project: Project,
    val name: String,
    private val ownConfig: GraphQLRawProjectConfig,
    private val defaultConfig: GraphQLRawProjectConfig?,
    val parent: GraphQLConfig,
) {
    val dir = parent.dir

    val file = parent.file

    val schema: List<GraphQLSchemaPointer> = (ownConfig.schema ?: defaultConfig?.schema ?: emptyList()).map {
        GraphQLSchemaPointer(project, dir, it, isLegacy, false)
    }

    val documents: List<String> = ownConfig.documents ?: defaultConfig?.documents ?: emptyList()

    val extensions: Map<String, Any?> = buildMap {
        defaultConfig?.extensions?.let { putAll(it) }
        ownConfig.extensions?.let { putAll(it) }
    }

    val include: List<String> = ownConfig.include ?: defaultConfig?.include ?: emptyList()

    val exclude: List<String> = ownConfig.exclude ?: defaultConfig?.exclude ?: emptyList()

    val scope = GraphQLScopeProvider.createScope(
        project, GraphQLConfigScope(project, GlobalSearchScope.projectScope(project), this)
    )

    val schemaScope = GraphQLScopeProvider.createScope(
        project, GraphQLConfigSchemaScope(project, GlobalSearchScope.projectScope(project), this)
    )

    private val endpointsLazy: Lazy<List<GraphQLConfigEndpoint>> = lazy { buildEndpoints() }

    val endpoints = endpointsLazy.value

    val isDefault = name == GraphQLConfig.DEFAULT_PROJECT

    private val matchingCache = GraphQLFileMatcherCache.newInstance(project)

    private val matchingSchemaCache = GraphQLFileMatcherCache.newInstance(project)

    fun matches(context: PsiFile): Boolean {
        return getPhysicalVirtualFile(context)?.let { matches(it) } ?: false
    }

    fun matches(virtualFile: VirtualFile): Boolean {
        return matchingCache.value.match(virtualFile, ::matchesImpl)
    }

    private fun matchesImpl(virtualFile: VirtualFile): Boolean {
        val isSchemaOrDocument = sequenceOf(schema, documents).any { matchPattern(virtualFile, it) }
        if (isSchemaOrDocument) {
            return true
        }

        return isIncluded(virtualFile)
    }

    fun matchesSchema(context: PsiFile): Boolean {
        return getPhysicalVirtualFile(context)?.let { matchesSchema(it) } ?: false
    }

    fun matchesSchema(virtualFile: VirtualFile): Boolean {
        return matchingSchemaCache.value.match(virtualFile, ::matchesSchemaImpl)
    }

    private fun matchesSchemaImpl(virtualFile: VirtualFile): Boolean {
        val isSchema = schema.any { matchPattern(virtualFile, it) }
        if (isSchema) {
            return true
        }

        // TODO: should we include SDL definitions included via `include` property or load just operations and fragments?
        //// in the legacy .graphqlconfig multiple schema files were provided via `includes`
        //return if (isLegacy) isIncluded(virtualFile) else false

        return isIncluded(virtualFile)
    }

    private fun isIncluded(virtualFile: VirtualFile): Boolean {
        val isExcluded = if (exclude.isNotEmpty()) matchPattern(virtualFile, exclude) else false
        if (isExcluded) {
            return false
        }

        return if (include.isNotEmpty()) matchPattern(virtualFile, include) else false
    }

    private fun matchPattern(candidate: VirtualFile, pointer: Any?): Boolean {
        return when (pointer) {
            is List<*> -> pointer.any { matchPattern(candidate, it) }
            is String -> GraphQLConfigGlobMatcher.getInstance(project).matches(candidate, pointer, dir)
            is GraphQLSchemaPointer -> matchPattern(candidate, pointer.globPath)
            else -> false
        }
    }

    val isLegacy
        get() = isLegacyConfig(file)

    private fun buildEndpoints(): List<GraphQLConfigEndpoint> {
        val endpointsMap =
            extensions[GraphQLConfigKeys.EXTENSION_ENDPOINTS] as? Map<*, *> ?: return emptyList()

        return endpointsMap.mapNotNull { (key: Any?, value: Any?) ->
            val endpointName = key as? String ?: return@mapNotNull null

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
                    val url = value["url"]
                    if (url is String) {
                        GraphQLRawEndpoint(
                            endpointName,
                            url,
                            value["introspect"] as Boolean? ?: false,
                            parseMap(value["headers"]) ?: emptyMap()
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
}
