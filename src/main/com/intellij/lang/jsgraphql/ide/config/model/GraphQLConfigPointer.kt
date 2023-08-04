package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile


data class GraphQLConfigPointer(val dir: VirtualFile?, val file: VirtualFile?, val projectName: String?) {
  fun resolve(project: Project): GraphQLProjectConfig? {
    val provider = GraphQLConfigProvider.getInstance(project)
    val config = provider.getForConfigFile(file ?: dir)
    return config?.findProject(projectName)
  }
}
