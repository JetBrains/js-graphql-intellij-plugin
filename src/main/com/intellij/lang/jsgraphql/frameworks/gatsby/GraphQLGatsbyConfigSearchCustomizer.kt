package com.intellij.lang.jsgraphql.frameworks.gatsby

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigSearchCustomizer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GraphQLGatsbyConfigSearchCustomizer : GraphQLConfigSearchCustomizer {
  override fun isIgnoredConfig(project: Project, file: VirtualFile): Boolean =
    isGatsbyGeneratedConfig(file.parent)

  private fun isGatsbyGeneratedConfig(parent: VirtualFile?): Boolean =
    parent?.name == "typegen" && parent.parent?.name == ".cache"
}