package com.intellij.lang.jsgraphql.schema.library

enum class GraphQLLibraryAttachmentScope {
  GLOBAL,
  PROJECT;

  companion object {
    @JvmField
    val ALL: Array<GraphQLLibraryAttachmentScope> = GraphQLLibraryAttachmentScope.entries.toTypedArray()
  }
}
