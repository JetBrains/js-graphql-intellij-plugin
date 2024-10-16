package com.intellij.lang.jsgraphql.ide.project.toolwindow

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLConstants
import com.intellij.lang.jsgraphql.ide.project.schemastatus.GraphQLSchemasPanel
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.impl.EditorHeaderComponent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.components.JBLabel
import com.intellij.ui.content.Content
import com.intellij.util.concurrency.ThreadingAssertions
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingConstants

class GraphQLToolWindow : ToolWindowFactory, DumbAware {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    createSchemasPanel(project, toolWindow)
    createToolWindowResultEditor(project, toolWindow)
  }

  private fun createSchemasPanel(project: Project, toolWindow: ToolWindow) {
    val schemasPanel = GraphQLSchemasPanel(project, toolWindow.id)
    val contentManager = toolWindow.contentManager
    val schemasContent = contentManager.factory
      .createContent(schemasPanel, GraphQLBundle.message("graphql.tab.title.schemas.project.structure"), false)
    schemasContent.isCloseable = false
    contentManager.addContent(schemasContent)
  }

  private fun createToolWindowResultEditor(project: Project, toolWindow: ToolWindow) {
    val virtualFile = LightVirtualFile("GraphQL.result.json", JsonFileType.INSTANCE, "")
    val fileEditor =
      PsiAwareTextEditorProvider().createEditor(project, virtualFile) as? TextEditor ?: return
    val editor = fileEditor.editor
    val editorEx = editor as EditorEx

    // set read-only mode
    editorEx.isViewer = true
    editorEx.settings.isShowIntentionBulb = false
    editor.getSettings().additionalLinesCount = 0
    editor.getSettings().isCaretRowShown = false
    editor.getSettings().isBlinkCaret = false

    val header = GraphQLQueryResultHeaderComponent(project)
    // finally, set the header as permanent such that it's restored after searches
    editor.setHeaderComponent(header)
    editorEx.permanentHeaderComponent = header

    val contentManager = toolWindow.contentManager
    val content = contentManager.factory
      .createContent(fileEditor.component, GraphQLBundle.message("graphql.tab.title.query.result"), true)
    content.isCloseable = false
    content.setShouldDisposeContent(false) // fileEditor will dispose the component itself
    content.setDisposer(fileEditor)
    content.putUserData(QUERY_RESULT_EDITOR_KEY, fileEditor)
    contentManager.addContent(content)
  }

  class GraphQLQueryResultHeaderComponent(project: Project) : EditorHeaderComponent() {
    val statusLabel = JBLabel().apply {
      isVisible = false
      iconTextGap = 0
    }

    val resultLabel: JBLabel = JBLabel("", null, SwingConstants.LEFT).apply {
      border = JBUI.Borders.empty(4, 6)
      cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
      isVisible = false
    }

    init {
      add(statusLabel, BorderLayout.WEST)
      add(resultLabel, BorderLayout.CENTER)

      resultLabel.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
          val fileUrl = resultLabel.getClientProperty(FILE_URL_PROPERTY) as? String
          if (fileUrl != null) {
            val queryFile = VirtualFileManager.getInstance().findFileByUrl(fileUrl)
            if (queryFile != null) {
              val fileEditorManager = FileEditorManager.getInstance(project)
              fileEditorManager.openFile(queryFile, true, true)
            }
          }
        }
      })
    }
  }

  companion object {
    private const val GRAPHQL_TOOL_WINDOW = GraphQLConstants.GraphQL

    const val GRAPHQL_TOOL_WINDOW_TOOLBAR = "GraphQLToolWindowToolbar"
    const val GRAPHQL_TOOL_WINDOW_POPUP = "GraphQLToolWindowPopup"

    const val FILE_URL_PROPERTY = "fileUrl"

    private val QUERY_RESULT_EDITOR_KEY = Key.create<TextEditor>("graphql.query.result.editor")

    @JvmStatic
    fun getQueryResultEditor(project: Project): TextEditor? {
      ThreadingAssertions.assertEventDispatchThread()
      val content = getQueryResultContent(project) ?: return null
      return content.getUserData(QUERY_RESULT_EDITOR_KEY)
    }

    private fun getQueryResultContent(project: Project): Content? {
      val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(GRAPHQL_TOOL_WINDOW) ?: return null
      return toolWindow.contentManager.findContent(GraphQLBundle.message("graphql.tab.title.query.result"))
    }

    @JvmStatic
    fun getQueryResultHeader(textEditor: TextEditor): GraphQLQueryResultHeaderComponent? {
      return textEditor.editor.headerComponent as? GraphQLQueryResultHeaderComponent
    }

    @JvmStatic
    fun showQueryResultEditor(project: Project) {
      runInEdt {
        val toolWindow =
          ToolWindowManager.getInstance(project).getToolWindow(GRAPHQL_TOOL_WINDOW) ?: return@runInEdt
        val content =
          toolWindow.contentManager.findContent(GraphQLBundle.message("graphql.tab.title.query.result")) ?: return@runInEdt
        toolWindow.show { toolWindow.contentManager.setSelectedContent(content) }
        val textEditor = content.getUserData(QUERY_RESULT_EDITOR_KEY)
        textEditor?.editor?.scrollingModel?.scrollVertically(0)
      }
    }
  }
}
