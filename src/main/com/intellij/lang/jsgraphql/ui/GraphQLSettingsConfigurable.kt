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
import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
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
        } else {
            ApplicationManager.getApplication().invokeLater({
                DaemonCodeAnalyzer.getInstance(project).restart()
                EditorNotifications.getInstance(project).updateAllNotifications()
            }, project.disposed)
        }
    }

    override fun createPanel(): DialogPanel {
        return panel {
            titledRow(message("graphql.settings.introspection")) {
                row(message("graphql.settings.introspection.query.label") + ":") {
                    expandableTextField(settings::getIntrospectionQuery, settings::setIntrospectionQuery)
                        .constraints(growX)
                        .applyToComponent {
                            emptyText.text = message("graphql.settings.introspection.query.empty.text")
                            toolTipText = message("graphql.settings.introspection.query.tooltip")
                        }
                }
                row {
                    checkBox(
                        message("graphql.settings.introspection.default.values.label"),
                        settings::isEnableIntrospectionDefaultValues,
                        settings::setEnableIntrospectionDefaultValues
                    ).applyToComponent { toolTipText = message("graphql.settings.introspection.default.values.tooltip") }
                }
                row {
                    checkBox(
                        message("graphql.settings.introspection.repeatable.directives.label"),
                        settings::isEnableIntrospectionRepeatableDirectives,
                        settings::setEnableIntrospectionRepeatableDirectives
                    ).applyToComponent { toolTipText = message("graphql.settings.introspection.repeatable.directives.tooltip") }
                }
                row {
                    checkBox(
                        message("graphql.settings.introspection.open.editor.label"),
                        settings::isOpenEditorWithIntrospectionResult,
                        settings::setOpenEditorWithIntrospectionResult
                    )
                }
            }

            titledRow(message("graphql.settings.frameworks")) {
                row {
                    checkBox(
                        message("graphql.settings.frameworks.relay.label"),
                        settings::isRelaySupportEnabled,
                        settings::setRelaySupportEnabled
                    )
                        .applyToComponent { toolTipText = message("graphql.settings.frameworks.relay.tooltip") }
                        .updateLibraries()
                }
                row {
                    checkBox(
                        message("graphql.settings.frameworks.federation.label"),
                        settings::isFederationSupportEnabled,
                        settings::setFederationSupportEnabled
                    ).updateLibraries()
                }
            }
        }
    }

    private fun <T : JComponent> CellBuilder<T>.updateLibraries(): CellBuilder<T> = onApply { shouldUpdateLibraries = true }

//    TODO: UI DSL V2, uncomment when sinceVersion >= 213
//    override fun createPanel(): DialogPanel {
//        return panel {
//            group(message("graphql.settings.introspection")) {
//                row(message("graphql.settings.introspection.query.label") + ":") {
//                    expandableTextField()
//                        .bindText(settings::getIntrospectionQuery, settings::setIntrospectionQuery)
//                        .horizontalAlign(HorizontalAlign.FILL)
//                        .applyToComponent {
//                            emptyText.text = message("graphql.settings.introspection.query.empty.text")
//                            toolTipText = message("graphql.settings.introspection.query.tooltip")
//                        }
//                }
//                row {
//                    checkBox(message("graphql.settings.introspection.default.values.label"))
//                        .bindSelected(settings::isEnableIntrospectionDefaultValues, settings::setEnableIntrospectionDefaultValues)
//                        .applyToComponent { toolTipText = message("graphql.settings.introspection.default.values.tooltip") }
//                }
//                row {
//                    checkBox(message("graphql.settings.introspection.repeatable.directives.label"))
//                        .bindSelected(
//                            settings::isEnableIntrospectionRepeatableDirectives,
//                            settings::setEnableIntrospectionRepeatableDirectives
//                        )
//                        .applyToComponent { toolTipText = message("graphql.settings.introspection.repeatable.directives.tooltip") }
//                }
//                row {
//                    checkBox(message("graphql.settings.introspection.open.editor.label"))
//                        .bindSelected(settings::isOpenEditorWithIntrospectionResult, settings::setOpenEditorWithIntrospectionResult)
//                }
//            }
//            group(message("graphql.settings.frameworks")) {
//                row {
//                    checkBox(message("graphql.settings.frameworks.relay.label"))
//                        .bindSelected(settings::isRelaySupportEnabled, settings::setRelaySupportEnabled)
//                        .applyToComponent { toolTipText = message("graphql.settings.frameworks.relay.tooltip") }
//                        .updateLibraries()
//                }
//                row {
//                    checkBox(message("graphql.settings.frameworks.federation.label"))
//                        .bindSelected(settings::isFederationSupportEnabled, settings::setFederationSupportEnabled)
//                        .updateLibraries()
//                }
//            }
//        }
//    }
//
//    private fun <T : JComponent> Cell<T>.updateLibraries(): Cell<T> = onApply { shouldUpdateLibraries = true }
}
