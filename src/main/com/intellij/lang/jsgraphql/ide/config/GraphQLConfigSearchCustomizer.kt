package com.intellij.lang.jsgraphql.ide.config

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface GraphQLConfigSearchCustomizer {
  companion object {
    @JvmField
    val EP_NAME =
      ExtensionPointName.create<GraphQLConfigSearchCustomizer>("com.intellij.lang.jsgraphql.configSearchCustomizer")
  }

  fun isIgnoredConfig(project: Project, file: VirtualFile): Boolean
}
