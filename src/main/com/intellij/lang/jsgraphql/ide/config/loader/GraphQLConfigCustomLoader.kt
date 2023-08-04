package com.intellij.lang.jsgraphql.ide.config.loader

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

interface GraphQLConfigCustomLoader {

  fun accepts(file: VirtualFile): Boolean

  fun load(project: Project, file: VirtualFile): Map<*, *>?

  companion object {
    @JvmField
    val EP_NAME =
      ExtensionPointName.create<GraphQLConfigCustomLoader>("com.intellij.lang.jsgraphql.configCustomLoader")

    fun forFile(file: VirtualFile): GraphQLConfigCustomLoader? {
      return EP_NAME.extensionList.find { it.accepts(file) }
    }
  }
}
