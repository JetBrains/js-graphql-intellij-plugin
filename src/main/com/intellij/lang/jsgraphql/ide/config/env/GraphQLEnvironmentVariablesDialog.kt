package com.intellij.lang.jsgraphql.ide.config.env

import com.intellij.execution.util.EnvVariablesTable
import com.intellij.execution.util.EnvironmentVariable
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.AnActionButtonRunnable
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellRenderer

class GraphQLEnvironmentVariablesDialog(
  project: Project,
  private val environment: GraphQLEnvironmentSnapshot,
  private val configFileOrDir: VirtualFile,
  private val onlyEmpty: Boolean,
  private val nameFilter: Collection<String>? = null,
) : DialogWrapper(project) {

  private val env = GraphQLConfigEnvironment.getInstance(project)

  private val table = createVariablesTable()

  init {
    title = GraphQLBundle.message("graphql.environment.variables.dialog.title")
    init()
  }

  override fun createCenterPanel(): JComponent {
    return JPanel(BorderLayout()).apply {
      add(table.component, BorderLayout.CENTER)
    }
  }

  override fun doOKAction() {
    val newVariables = table.environmentVariables
      .filterNot { it.name.isNullOrBlank() }
      .associate { it.name to it.value }
    env.setExplicitVariables(newVariables, configFileOrDir)

    super.doOKAction()
  }

  override fun getDimensionServiceKey() = "GraphQLEnvironmentVariablesDialog"

  override fun getInitialSize(): Dimension = super.getInitialSize() ?: Dimension(500, 500)

  private fun createVariablesTable(): EnvVariablesTable {
    return environment.variables
      .asSequence()
      .filter {
        nameFilter == null || it.key in nameFilter
      }
      .filter {
        if (onlyEmpty) {
          it.value.isNullOrBlank()
        }
        else {
          true
        }
      }
      .map {
        EnvironmentVariable(it.key, env.getExplicitVariable(it.key, configFileOrDir), false)
      }
      .toList()
      .let { MyEnvVariablesTable(it) }
  }

  private inner class MyEnvVariablesTable(list: List<EnvironmentVariable>) : EnvVariablesTable() {
    init {
      tableView.visibleRowCount = JBTable.PREFERRED_SCROLLABLE_VIEWPORT_HEIGHT_IN_ROWS
      setValues(list)
    }

    override fun createAddAction(): AnActionButtonRunnable? {
      return null
    }

    override fun createRemoveAction(): AnActionButtonRunnable? {
      return null
    }

    override fun createListModel(): ListTableModel<EnvironmentVariable> {
      return ListTableModel<EnvironmentVariable>(MyNameColumnInfo(), MyValueColumnInfo())
    }

    private inner class MyNameColumnInfo : NameColumnInfo() {
      override fun isCellEditable(environmentVariable: EnvironmentVariable?): Boolean {
        return false
      }
    }

    private inner class MyValueColumnInfo : ValueColumnInfo() {
      private val myModifiedRenderer: DefaultTableCellRenderer = object : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
          table: JTable?,
          value: Any?,
          isSelected: Boolean,
          hasFocus: Boolean,
          row: Int,
          column: Int,
        ): Component {
          val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
          component.font = component.font.deriveFont(Font.BOLD)
          if (!hasFocus && !isSelected) {
            component.foreground = JBUI.CurrentTheme.Link.Foreground.ENABLED
          }
          return component
        }
      }

      override fun getCustomizedRenderer(o: EnvironmentVariable, renderer: TableCellRenderer): TableCellRenderer {
        val showModified = if (onlyEmpty) {
          o.value != null
        }
        else {
          environment.variables[o.name] != o.value
        }

        return if (showModified) myModifiedRenderer else renderer
      }
    }
  }
}
