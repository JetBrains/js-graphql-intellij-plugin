package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.getPhysicalVirtualFile
import com.intellij.lang.jsgraphql.ide.config.isLegacyConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
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
    private val data: GraphQLRawConfig,
) {
    companion object {
        const val DEFAULT_PROJECT = "default"
    }

    private val projects: Map<String, GraphQLProjectConfig> = initProjects()

    /**
     * NULL config to store as weak referenced value in [fileToProjectCache]. Shouldn't be exposed to the outside of the class.
     */
    private val nullConfig = GraphQLProjectConfig(project, "NULL", GraphQLRawProjectConfig.EMPTY, null, this)

    private val fileToProjectCache: CachedValue<ConcurrentMap<VirtualFile, GraphQLProjectConfig>> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                ContainerUtil.createConcurrentWeakValueMap(),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
        }

    private fun initProjects(): Map<String, GraphQLProjectConfig> {
        val root = GraphQLRawProjectConfig(data.schema, data.documents, data.extensions, data.include, data.exclude)

        return if (data.projects.isNullOrEmpty()) {
            mapOf(DEFAULT_PROJECT to GraphQLProjectConfig(project, DEFAULT_PROJECT, root, null, this))
        } else {
            data.projects.mapValues { (name, config) ->
                GraphQLProjectConfig(project, name, config, root, this)
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

    /**
     * Empty if it doesn't contain any explicit file patterns,
     * so it becomes a default config for the files under this root.
     */
    val isEmpty: Boolean = data.schema.isNullOrEmpty() &&
        data.projects.isNullOrEmpty() &&
        data.include.isNullOrEmpty() &&
        data.exclude.isNullOrEmpty()

    fun match(context: PsiFile): GraphQLProjectConfig? {
        return getPhysicalVirtualFile(context)?.let { match(it) }
    }

    fun match(virtualFile: VirtualFile): GraphQLProjectConfig? {
        val cache = fileToProjectCache.value
        val cachedResult = cache[virtualFile]
        if (cachedResult != null) {
            return cachedResult.takeIf { it !== nullConfig }
        }

        val result = findProjectForFile(virtualFile) ?: nullConfig
        return (cache.putIfAbsent(virtualFile, result) ?: result).takeIf { it !== nullConfig }
    }

    private fun findProjectForFile(virtualFile: VirtualFile): GraphQLProjectConfig? {
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

}


