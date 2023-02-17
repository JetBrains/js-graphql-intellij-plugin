package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeDependency
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentMap


data class GraphQLConfig(
    private val project: Project,
    val dir: VirtualFile,
    val file: VirtualFile?,
    val rawData: GraphQLRawConfig,
) {
    companion object {
        const val DEFAULT_PROJECT = "default"
    }

    private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)

    /**
     * Empty if it doesn't contain any explicit file patterns,
     * so it becomes a default config for the files under this root.
     */
    val isEmpty: Boolean = rawData.schema.isNullOrEmpty() &&
        rawData.projects.isNullOrEmpty() &&
        rawData.include.isNullOrEmpty() &&
        rawData.exclude.isNullOrEmpty()

    private val projects: Map<String, GraphQLProjectConfig> = initProjects()

    /**
     * NULL config to store as weak referenced value in [fileToProjectCache]. Shouldn't be exposed to the outside of the class.
     */
    private val nullConfig = GraphQLProjectConfig(project, "NULL", GraphQLRawProjectConfig.EMPTY, null, dir, file, isEmpty)

    private val fileToProjectCache: CachedValue<ConcurrentMap<VirtualFile, GraphQLProjectConfig>> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                ContainerUtil.createConcurrentWeakValueMap(),
                GraphQLScopeDependency.getInstance(project),
            )
        }

    private fun initProjects(): Map<String, GraphQLProjectConfig> {
        val root = GraphQLRawProjectConfig(rawData.schema, rawData.documents, rawData.extensions, rawData.include, rawData.exclude)

        return if (rawData.projects.isNullOrEmpty()) {
            mapOf(DEFAULT_PROJECT to GraphQLProjectConfig(project, DEFAULT_PROJECT, root, null, dir, file, isEmpty))
        } else {
            rawData.projects.mapValues { (name, config) ->
                GraphQLProjectConfig(project, name, config, root, dir, file, isEmpty)
            }
        }
    }

    fun findProject(name: String? = null): GraphQLProjectConfig? {
        return if (name == null) getDefault() else projects[name]
    }

    fun getProjects(): Map<String, GraphQLProjectConfig> {
        return LinkedHashMap(projects)
    }

    fun getDefault(): GraphQLProjectConfig? {
        return projects[DEFAULT_PROJECT]
    }

    fun hasOnlyDefaultProject(): Boolean {
        return projects.size == 1 && getDefault() != null
    }

    val isLegacy: Boolean
        get() = isLegacyConfig(file)

    fun match(context: PsiFile): GraphQLProjectConfig? {
        return getPhysicalVirtualFile(context)?.let { match(it) }
    }

    private fun match(virtualFile: VirtualFile): GraphQLProjectConfig? {
        val cache = fileToProjectCache.value
        val cachedResult = cache[virtualFile]
        if (cachedResult != null) {
            return cachedResult.takeIf { it !== nullConfig }
        }

        val result = findProjectForFile(virtualFile) ?: nullConfig
        return (cache.putIfAbsent(virtualFile, result) ?: result).takeIf { it !== nullConfig }
    }

    private fun findProjectForFile(virtualFile: VirtualFile): GraphQLProjectConfig? {
        if (requiresSchemaStrictMatch(virtualFile)) {
            for (config in projects.values) {
                // more strict than matching for regular project files
                if (config.matchesSchema(virtualFile)) {
                    return config
                }
            }

            return null
        }

        for (config in projects.values) {
            if (config.matches(virtualFile)) {
                return config
            }
        }

        for (config in projects.values) {
            if (config.include.isEmpty() && config.exclude.isEmpty()) {
                return config
            }
        }

        return null
    }

    private fun requiresSchemaStrictMatch(virtualFile: VirtualFile) =
        virtualFile.fileType == JsonFileType.INSTANCE ||
            generatedSourcesManager.isGeneratedFile(virtualFile) ||
            generatedSourcesManager.isSourceForGeneratedFile(virtualFile)
}


