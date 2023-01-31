package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

private const val DEFAULT_PROJECT = "default"

class GraphQLConfig(
    private val project: Project,
    val file: VirtualFile,
    private val rawConfig: GraphQLRawConfig,
) {
    companion object {
        private val MATCHING_PROJECT_KEY =
            Key.create<CachedValue<GraphQLProjectConfig?>>("graphql.file.matching.project")
    }

    private val projects: Map<String, GraphQLProjectConfig> = initProjects()

    private fun initProjects(): Map<String, GraphQLProjectConfig> {
        return if (rawConfig.projects.isEmpty()) {
            mapOf(
                DEFAULT_PROJECT to GraphQLProjectConfig(
                    project,
                    file,
                    DEFAULT_PROJECT,
                    rawConfig.root,
                    null
                )
            )
        } else {
            rawConfig.projects.mapValues { (name, config) ->
                GraphQLProjectConfig(project, file, name, config, rawConfig.root)
            }
        }
    }

    fun findProject(name: String? = null): GraphQLProjectConfig? {
        return if (name == null) getDefault() else projects[name]
    }

    fun findProjectForFile(context: PsiFile): GraphQLProjectConfig? {
        val projectConfig = CachedValuesManager.getCachedValue(context, MATCHING_PROJECT_KEY) {
            CachedValueProvider.Result.create(
                findProjectForFileImpl(context),
                context,
                GraphQLConfigProvider.getInstance(project),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
        }

        // a file should have only one matching config therefore
        // a cached value could be stored in the psi and shared between different configs,
        // so we need to check if the result is from this exact config
        return projectConfig?.takeIf {
            findProject(it.name) == projectConfig
        }
    }

    private fun findProjectForFileImpl(context: PsiFile): GraphQLProjectConfig? {
        for (config in projects.values) {
            if (config.match(context)) {
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

    fun getDefault(): GraphQLProjectConfig? {
        return projects[DEFAULT_PROJECT]
    }

    fun isLegacy(): Boolean {
        return isLegacyConfig(file)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphQLConfig

        if (project != other.project) return false
        if (file != other.file) return false
        if (rawConfig != other.rawConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = project.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + rawConfig.hashCode()
        return result
    }

}

fun isLegacyConfig(file: VirtualFile): Boolean {
    return file.name.lowercase() in LEGACY_CONFIG_NAMES
}
