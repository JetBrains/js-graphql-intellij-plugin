package com.intellij.lang.jsgraphql.schema.library

import com.intellij.openapi.project.Project

abstract class GraphQLLibraryDescriptor(
  val identifier: String,
  val displayName: String = identifier,
  val description: String? = null,
  val attachmentScope: GraphQLLibraryAttachmentScope = GraphQLLibraryAttachmentScope.GLOBAL,
) {
  abstract fun isEnabled(project: Project): Boolean

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GraphQLLibraryDescriptor

    if (identifier != other.identifier) return false
    if (displayName != other.displayName) return false
    if (description != other.description) return false
    if (attachmentScope != other.attachmentScope) return false

    return true
  }

  override fun hashCode(): Int {
    var result = identifier.hashCode()
    result = 31 * result + displayName.hashCode()
    result = 31 * result + (description?.hashCode() ?: 0)
    result = 31 * result + attachmentScope.hashCode()
    return result
  }
}
