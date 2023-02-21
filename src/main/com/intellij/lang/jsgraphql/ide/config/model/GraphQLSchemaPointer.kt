package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEnvironmentSnapshot
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.URLUtil

private val invalidPathCharsRegex = Regex("[{}$*,]")

data class GraphQLSchemaPointer(
    private val project: Project,
    private val dir: VirtualFile,
    val rawData: GraphQLRawSchemaPointer,
    private val isLegacy: Boolean,
    private val environment: GraphQLEnvironmentSnapshot,
) {
    val pathOrUrl: String =
        expandVariables(rawData.pathOrUrl, createExpandContext()) ?: rawData.pathOrUrl

    val headers: Map<String, Any?> =
        parseMap(expandVariables(rawData.headers, createExpandContext())) ?: emptyMap()

    val isRemote: Boolean = URLUtil.canContainUrl(pathOrUrl)

    val filePath: String? = pathOrUrl.takeUnless { URLUtil.canContainUrl(it) || it.contains(invalidPathCharsRegex) }

    val globPath: String? = pathOrUrl.takeUnless { URLUtil.canContainUrl(it) || it.contains('$') }

    fun withCurrentEnvironment(): GraphQLSchemaPointer {
        val snapshot = GraphQLConfigEnvironment.getInstance(project).createSnapshot(environment.variables.keys, dir)
        return copy(environment = snapshot)
    }

    private fun createExpandContext() = GraphQLExpandVariableContext(project, dir, isLegacy, environment)
}
