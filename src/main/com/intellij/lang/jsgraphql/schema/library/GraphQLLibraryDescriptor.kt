package com.intellij.lang.jsgraphql.schema.library

import com.intellij.openapi.project.Project

internal val GRAPHQL_EMPTY_LIBRARY_DESCRIPTOR = object : GraphQLLibraryDescriptor("EMPTY") {
  override fun isEnabled(project: Project): Boolean {
    return false
  }
}

abstract class GraphQLLibraryDescriptor(
  val identifier: String,
  val displayName: String = identifier,
  val description: String? = null,
) {
  abstract fun isEnabled(project: Project): Boolean

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GraphQLLibraryDescriptor

    if (identifier != other.identifier) return false
    if (displayName != other.displayName) return false
    if (description != other.description) return false

    return true
  }

  override fun hashCode(): Int {
    var result = identifier.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + (description?.hashCode() ?: 0)
    return result
  }
}
