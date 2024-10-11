@file:JvmName("GraphQLUtil")

package com.intellij.lang.jsgraphql

import com.intellij.ide.scratch.ScratchRootType
import com.intellij.lang.jsgraphql.ide.config.CONFIG_OVERRIDE_COMMENT
import com.intellij.lang.jsgraphql.ide.config.CONFIG_OVERRIDE_COMMENT_LEGACY
import com.intellij.lang.jsgraphql.ide.config.CONFIG_OVERRIDE_PROJECT_SEP
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.ui.GraphQLUIProjectService
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.concurrency.annotations.RequiresEdt
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

const val GRAPHQL_CACHE_DIR_NAME = "graphql"

@OptIn(ExperimentalContracts::class)
@Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
inline fun <reified T : Any> Any?.asSafely(): @kotlin.internal.NoInfer T? {
  contract {
    returnsNotNull() implies (this@asSafely is T)
  }
  return this as? T
}

@RequiresEdt
@JvmOverloads
fun createScratchFromEndpoint(
  project: Project,
  endpoint: GraphQLConfigEndpoint,
  openInEditor: Boolean = false,
) {
  val scratchFile = createScratchFile(project, endpoint.file?.path ?: endpoint.dir.path, endpoint.projectName)
  if (openInEditor && scratchFile != null) {
    val fileEditors = FileEditorManager.getInstance(project).openFile(scratchFile, true)
    for (editor in fileEditors) {
      if (editor is TextEditor) {
        val endpointsModel = editor.editor.getUserData(GraphQLUIProjectService.GRAPH_QL_ENDPOINTS_MODEL)
        endpointsModel?.selectedItem = endpoint
      }
    }
  }
}

@RequiresEdt
fun createScratchFile(
  project: Project,
  path: String,
  projectName: String?,
): VirtualFile? {
  val comment = createOverrideConfigComment(path, projectName)
  val text =
    """
        $comment

        query ScratchQuery {

        }
        """.trimIndent()
  return ScratchRootType.getInstance().createScratchFile(project, "scratch.graphql", GraphQLLanguage.INSTANCE, text)
}

fun createOverrideConfigComment(
  path: String,
  projectName: String?,
): String {
  return buildString {
    append("# ")
    append(CONFIG_OVERRIDE_COMMENT)
    append(FileUtil.toSystemDependentName(path))
    projectName
      .takeIf { !it.isNullOrEmpty() && it != GraphQLConfig.DEFAULT_PROJECT }
      ?.let {
        append(CONFIG_OVERRIDE_PROJECT_SEP)
        append(it)
      }
  }
}

fun parseOverrideConfigComment(str: String): GraphQLConfigOverridePath? {
  var fromIdx = str.indexOf(CONFIG_OVERRIDE_COMMENT)
  var prefixLength = CONFIG_OVERRIDE_COMMENT.length

  if (fromIdx == -1) {
    fromIdx = str.indexOf(CONFIG_OVERRIDE_COMMENT_LEGACY)
    prefixLength = CONFIG_OVERRIDE_COMMENT_LEGACY.length
  }

  if (fromIdx == -1) return null
  val override = str.substring(fromIdx + prefixLength).trim().takeIf { it.isNotEmpty() } ?: return null

  val projectSepIdx = override.lastIndexOf(CONFIG_OVERRIDE_PROJECT_SEP)
  return if (projectSepIdx != -1) {
    val path = override.substring(0, projectSepIdx).trim().let { FileUtil.toSystemIndependentName(it) }
    if (path.isEmpty()) {
      return null
    }
    val projectName = override.substring(projectSepIdx + 1).trim().takeIf { it.isNotEmpty() }
    GraphQLConfigOverridePath(path, projectName)
  }
  else {
    GraphQLConfigOverridePath(FileUtil.toSystemIndependentName(override), null)
  }
}

data class GraphQLConfigOverridePath(val path: String, val project: String?)
