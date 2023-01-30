package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawProjectConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLSchemaPointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

class GraphQLProjectConfig(
    private val project: Project,
    val file: VirtualFile,
    val name: String,
    private val rawConfig: GraphQLRawProjectConfig,
    private val rootConfig: GraphQLRawProjectConfig?
) {
    val schema: List<GraphQLSchemaPointer> = rawConfig.schema ?: rootConfig?.schema ?: emptyList()

    val documents: List<String> = rawConfig.documents ?: rootConfig?.documents ?: emptyList()

    val extensions: Map<String, Any?> = buildMap {
        rootConfig?.extensions?.let { putAll(it) }
        rawConfig.extensions?.let { putAll(it) }
    }

    val include: List<String> = rawConfig.include ?: rootConfig?.include ?: emptyList()

    val exclude: List<String> = rawConfig.exclude ?: rootConfig?.exclude ?: emptyList()

    fun matches(context: PsiFile): Boolean {
        TODO()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GraphQLProjectConfig

        if (project != other.project) return false
        if (file != other.file) return false
        if (name != other.name) return false
        if (rawConfig != other.rawConfig) return false
        if (rootConfig != other.rootConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = project.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + rawConfig.hashCode()
        result = 31 * result + (rootConfig?.hashCode() ?: 0)
        return result
    }

}
