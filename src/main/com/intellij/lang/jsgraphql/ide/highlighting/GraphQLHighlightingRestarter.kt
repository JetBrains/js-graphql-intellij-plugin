package com.intellij.lang.jsgraphql.ide.highlighting

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.jsgraphql.GraphQLExecutionMode
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.TestOnly

/**
 * Restarts the highlighting of the project files after the GraphQL configuration or schema has changed.
 * It is the only place of the plugin that calls [DaemonCodeAnalyzer].
 * A no-op in [GraphQLExecutionMode.isSynchronous] mode: there is no editor to re-highlight, and the server has no daemon.
 * A light service must be final, so a test replaces it with an instance of [createForTests] to observe the restarts.
 */
@Service(Service.Level.PROJECT)
class GraphQLHighlightingRestarter(private val project: Project) {
  companion object {
    @JvmStatic
    fun getInstance(project: Project): GraphQLHighlightingRestarter = project.service()

    /**
     * Creates a restarter that calls [onRestart] instead of the daemon. Pass it to `project.replaceService`.
     */
    @TestOnly
    fun createForTests(project: Project, onRestart: (reason: String) -> Unit): GraphQLHighlightingRestarter {
      return GraphQLHighlightingRestarter(project).also { it.onRestartForTests = onRestart }
    }
  }

  // set once by createForTests before the instance is published
  private var onRestartForTests: ((String) -> Unit)? = null

  fun restart(reason: String) {
    val onRestart = onRestartForTests
    if (onRestart != null) {
      onRestart(reason)
      return
    }

    if (GraphQLExecutionMode.isSynchronous) {
      return
    }
    DaemonCodeAnalyzer.getInstance(project).restart(reason)
  }
}
