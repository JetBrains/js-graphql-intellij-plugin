package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

private const val DEFAULT_PROJECT = "default"

class GraphQLConfig(
    private val project: Project,
    val file: VirtualFile,
    private val rawConfig: GraphQLRawConfig,
) {

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
        for (config in projects.values) {
            if (config.matches(context)) {
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
