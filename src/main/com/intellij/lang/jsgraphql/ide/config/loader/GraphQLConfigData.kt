package com.intellij.lang.jsgraphql.ide.config.loader


data class GraphQLRawConfig(
  val schema: List<GraphQLRawSchemaPointer>? = null,
  val documents: List<String>? = null,
  val extensions: Map<String, Any?>? = null,
  val include: List<String>? = null,
  val exclude: List<String>? = null,
  val projects: Map<String, GraphQLRawProjectConfig>? = null,
) {
  constructor(
    root: GraphQLRawProjectConfig = GraphQLRawProjectConfig.EMPTY,
    projects: Map<String, GraphQLRawProjectConfig>? = null
  ) : this(
    root.schema, root.documents, root.extensions, root.include, root.exclude, projects
  )

  companion object {
    @JvmField
    val EMPTY = GraphQLRawConfig()
  }
}

data class GraphQLRawProjectConfig(
  val schema: List<GraphQLRawSchemaPointer>? = null,
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

data class GraphQLRawSchemaPointer(
  val pattern: String,
  val headers: Map<String, Any?> = emptyMap(),
  val introspect: Boolean? = false,
)

data class GraphQLRawEndpoint(
  val name: String? = null,
  val url: String? = null,
  val headers: Map<String, Any?> = emptyMap(),
  val introspect: Boolean? = false,
)
