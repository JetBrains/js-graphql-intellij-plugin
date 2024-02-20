package com.intellij.lang.jsgraphql.ui

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.lang.jsgraphql.GraphQLBundle.message
import com.intellij.lang.jsgraphql.GraphQLConstants
import com.intellij.lang.jsgraphql.GraphQLSettings
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.EditorNotifications
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

private const val CONFIGURABLE_ID = "settings.jsgraphql"

class GraphQLSettingsConfigurable(private val project: Project) :
  BoundSearchableConfigurable(GraphQLConstants.GraphQL, CONFIGURABLE_ID, CONFIGURABLE_ID) {

  private val settings = GraphQLSettings.getSettings(project)
  private var shouldUpdateLibraries = false

  override fun apply() {
    shouldUpdateLibraries = false // updated in the super.apply()

    super.apply()

    if (shouldUpdateLibraries) {
      GraphQLLibraryManager.getInstance(project).notifyLibrariesChanged()
    }
    else {
      ApplicationManager.getApplication().invokeLater({
                                                        DaemonCodeAnalyzer.getInstance(project).restart()
                                                        EditorNotifications.getInstance(project).updateAllNotifications()
                                                      }, project.disposed)
    }
  }

  override fun createPanel(): DialogPanel {
    return panel {
      group(message("graphql.settings.introspection")) {
        row(message("graphql.settings.introspection.query.label") + ":") {
          expandableTextField()
            .bindText(settings::introspectionQuery)
            .align(AlignX.FILL)
            .applyToComponent {
              emptyText.text = message("graphql.settings.introspection.query.empty.text")
              toolTipText = message("graphql.settings.introspection.query.tooltip")
            }
        }
        row {
          checkBox(message("graphql.settings.introspection.default.values.label"))
            .bindSelected(settings::isEnableIntrospectionDefaultValues)
            .applyToComponent {
              toolTipText = message("graphql.settings.introspection.default.values.tooltip")
            }
        }
        row {
          checkBox(message("graphql.settings.introspection.repeatable.directives.label"))
            .bindSelected(settings::isEnableIntrospectionRepeatableDirectives)
            .applyToComponent {
              toolTipText = message("graphql.settings.introspection.repeatable.directives.tooltip")
            }
        }
        row {
          checkBox(message("graphql.settings.introspection.open.editor.label"))
            .bindSelected(settings::isOpenEditorWithIntrospectionResult)
        }
      }
      group(message("graphql.settings.frameworks")) {
        row {
          checkBox(message("graphql.library.relay"))
            .bindSelected(settings::isRelaySupportEnabled)
            .updateLibraries()
        }
        row {
          checkBox(message("graphql.library.federation"))
            .bindSelected(settings::isFederationSupportEnabled)
            .updateLibraries()
        }
        row {
          @Suppress("DialogTitleCapitalization")
          checkBox(message("graphql.library.apollokotlin"))
            .bindSelected(settings::isApolloKotlinSupportEnabled)
            .updateLibraries()
        }
      }
    }
  }

  private fun <T : JComponent> Cell<T>.updateLibraries(): Cell<T> = onApply { shouldUpdateLibraries = true }
}
