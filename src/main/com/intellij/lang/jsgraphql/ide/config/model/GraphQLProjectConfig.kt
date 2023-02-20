package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEnvironmentSnapshot
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigGlobMatcher
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigSchemaScope
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLConfigScope
import com.intellij.lang.jsgraphql.ide.config.scope.GraphQLFileMatcherCache
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager


data class GraphQLProjectConfig(
    private val project: Project,
    val name: String,
    private val ownConfig: GraphQLRawProjectConfig,
    private val defaultConfig: GraphQLRawProjectConfig?,
    val dir: VirtualFile,
    val file: VirtualFile?,
    val isRootEmpty: Boolean,
    val environment: GraphQLEnvironmentSnapshot,
) {
    private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)

    val isLegacy = isLegacyConfig(file)

    val schema: List<GraphQLSchemaPointer> =
        (ownConfig.schema ?: defaultConfig?.schema)?.map { GraphQLSchemaPointer(project, dir, it, isLegacy, environment) } ?: emptyList()

    val documents: List<String> =
        (ownConfig.documents ?: defaultConfig?.documents)?.let { expandVariables(it, expandContext) } ?: emptyList()

    val extensions: Map<String, Any?> = buildMap {
        defaultConfig?.extensions?.let { putAll(it) }
        ownConfig.extensions?.let { putAll(it) }
    }

    val include: List<String> =
        (ownConfig.include ?: defaultConfig?.include)?.let { expandVariables(it, expandContext) } ?: emptyList()

    val exclude: List<String> =
        (ownConfig.exclude ?: defaultConfig?.exclude)?.let { expandVariables(it, expandContext) } ?: emptyList()

    private val endpointsLazy: Lazy<List<GraphQLConfigEndpoint>> = lazy { buildEndpoints() }

    val endpoints = endpointsLazy.value

    val isDefault = name == GraphQLConfig.DEFAULT_PROJECT

    // need to create a new one for each invocation, because it has its own state
    private val expandContext
        get() = GraphQLExpandVariableContext(project, dir, isLegacy, environment)

    private val matchingCache = GraphQLFileMatcherCache.newInstance(project)

    private val matchingSchemaCache = GraphQLFileMatcherCache.newInstance(project)

    private val baseScope
        get() = GlobalSearchScope
            .projectScope(project)
            .union(generatedSourcesManager.createGeneratedSourcesScope())

    private val scopeCached: CachedValue<GlobalSearchScope> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                GraphQLScopeProvider.createScope(project, GraphQLConfigScope(project, baseScope, this)),
                GraphQLScopeDependency.getInstance(project),
            )
        }

    val scope: GlobalSearchScope
        get() = scopeCached.value

    private val schemaScopeCached: CachedValue<GlobalSearchScope> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                GraphQLScopeProvider.createScope(project, GraphQLConfigSchemaScope(project, baseScope, this)),
                GraphQLScopeDependency.getInstance(project),
            )
        }

    val schemaScope: GlobalSearchScope
        get() = schemaScopeCached.value

    val parentConfig: GraphQLConfig?
        get() = GraphQLConfigProvider.getInstance(project).getForConfigFile(file ?: dir)

    fun matches(context: PsiFile): Boolean {
        return getPhysicalVirtualFile(context)?.let { matches(it) } ?: false
    }

    fun matches(virtualFile: VirtualFile): Boolean {
        return matchingCache.value.match(virtualFile, ::matchesImpl)
    }

    private fun matchesImpl(virtualFile: VirtualFile): Boolean {
        if (generatedSourcesManager.isGeneratedFile(virtualFile)) {
            return generatedSourcesManager.getSourceFile(virtualFile)?.let { matches(it) } ?: false
        }

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
        if (generatedSourcesManager.isGeneratedFile(virtualFile)) {
            return generatedSourcesManager.getSourceFile(virtualFile)?.let { matchesSchema(it) } ?: false
        }

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
                        emptyMap(),
                        false,
                    )
                }

                is Map<*, *> -> {
                    val url = value[GraphQLConfigKeys.EXTENSION_ENDPOINT_URL]
                    if (url is String) {
                        GraphQLRawEndpoint(
                            endpointName,
                            url,
                            parseMap(value[GraphQLConfigKeys.HEADERS]) ?: emptyMap(),
                            value[GraphQLConfigKeys.INTROSPECT] as Boolean?
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
                GraphQLConfigPointer(file ?: dir, name),
                isLegacy,
                environment,
            )
        }
    }
}
