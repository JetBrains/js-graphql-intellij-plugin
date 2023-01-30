package com.intellij.lang.jsgraphql.ide.config.loader

import com.intellij.util.io.URLUtil


data class GraphQLRawConfig(
    val root: GraphQLRawProjectConfig,
    val projects: Map<String, GraphQLRawProjectConfig> = emptyMap(),
)

data class GraphQLRawProjectConfig(
    val schema: List<GraphQLSchemaPointer>?,
    val documents: List<String>?,
    val extensions: Map<String, Any?>?,
    val include: List<String>?,
    val exclude: List<String>?,
)

data class GraphQLSchemaPointer(
    val pathOrUrl: String,
    val headers: Map<String, String> = emptyMap()
) {
    val isRemote: Boolean = URLUtil.canContainUrl(pathOrUrl)
}
