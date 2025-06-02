package com.intellij.lang.jsgraphql

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class GraphQLCoroutineScope(private val cs: CoroutineScope) {
  companion object {
    @JvmStatic
    fun get(project: Project): CoroutineScope = project.service<GraphQLCoroutineScope>().cs
  }
}