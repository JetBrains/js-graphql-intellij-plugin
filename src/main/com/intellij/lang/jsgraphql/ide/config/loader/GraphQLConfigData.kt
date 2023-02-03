package com.intellij.lang.jsgraphql.ide.config.loader

import com.intellij.util.io.URLUtil


data class GraphQLRawConfig(
    val root: GraphQLRawProjectConfig = GraphQLRawProjectConfig.EMPTY,
    val projects: Map<String, GraphQLRawProjectConfig> = emptyMap(),
) {
    companion object {
        @JvmField
        val EMPTY = GraphQLRawConfig()
    }
}

data class GraphQLRawProjectConfig(
    val schema: List<GraphQLSchemaPointer>? = null,
    val documents: List<String>? = null,
    val extensions: Map<String, Any?>? = null,
    val include: List<String>? = null,
    val exclude: List<String>? = null,
) {
    companion object {
        @JvmField
        val EMPTY = GraphQLRawProjectConfig()
    }
}

data class GraphQLSchemaPointer(
    val pathOrUrl: String,
    val headers: Map<String, Any?> = emptyMap()
) {
    val isRemote: Boolean = URLUtil.canContainUrl(pathOrUrl)
}

data class GraphQLRawEndpoint(
    var name: String? = null,
    var url: String? = null,
    var introspect: Boolean = false,
    var headers: Map<String, Any?> = emptyMap()
)
