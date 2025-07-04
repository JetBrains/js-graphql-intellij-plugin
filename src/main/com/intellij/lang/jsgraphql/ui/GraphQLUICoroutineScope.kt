package com.intellij.lang.jsgraphql.ui

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.PROJECT)
class GraphQLUICoroutineScope(private val coroutineScope: CoroutineScope) {
  companion object {
    @JvmStatic
    fun get(project: Project): CoroutineScope = project.service<GraphQLUICoroutineScope>().coroutineScope
  }
}