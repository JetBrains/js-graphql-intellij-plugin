package com.intellij.lang.jsgraphql.ide.introspection

enum class GraphQLSchemaCapabilitiesDetectionStrategy(private val displayName: String) {
  ADAPTIVE("Adaptive"),
  LATEST("Latest"),
  LEGACY("Legacy");

  override fun toString(): String = displayName
}