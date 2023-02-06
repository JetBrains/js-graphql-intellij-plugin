package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironmentParser
import com.intellij.lang.jsgraphql.ide.config.expandVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.URLUtil

data class GraphQLSchemaPointer(
    private val project: Project,
    private val dir: VirtualFile,
    private val data: GraphQLRawSchemaPointer,
    private val isLegacy: Boolean,
    private val isUIContext: Boolean,
) {
    val pathOrUrl: String
        get() = if (pathContainsVariables) {
            expandVariables(project, data.pathOrUrl, dir, isLegacy, isUIContext)
        } else {
            data.pathOrUrl
        }

    val headers: Map<String, Any?>
        get() = expandVariables(project, data.headers, dir, isLegacy, isUIContext)

    val pathContainsVariables =
        GraphQLConfigEnvironmentParser.getInstance(project).containsVariables(data.pathOrUrl, isLegacy)

    val isRemote: Boolean
        get() = URLUtil.canContainUrl(pathOrUrl)

    private val invalidPathCharsRegex = Regex("[{}$*,]")

    val filePath: String?
        get() = pathOrUrl.takeUnless { URLUtil.canContainUrl(it) || it.contains(invalidPathCharsRegex) }

    val globPath: String?
        get() = pathOrUrl.takeUnless { URLUtil.canContainUrl(it) || it.contains('$') }

    fun withUIContext(newIsUIContext: Boolean): GraphQLSchemaPointer {
        return copy(isUIContext = newIsUIContext)
    }
}
