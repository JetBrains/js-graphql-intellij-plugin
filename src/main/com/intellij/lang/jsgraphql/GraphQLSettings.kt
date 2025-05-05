/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql

import com.intellij.lang.jsgraphql.GraphQLSettings.GraphQLSettingsState
import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Project-wide GraphQL settings persisted in the .idea folder as graphql-settings.xml
 */
@Service(Service.Level.PROJECT)
@State(name = "GraphQLSettings", storages = [Storage("graphql-settings.xml")])
class GraphQLSettings : PersistentStateComponent<GraphQLSettingsState> {

  private var state = GraphQLSettingsState()

  override fun getState(): GraphQLSettingsState = state

  override fun loadState(newState: GraphQLSettingsState) {
    state = newState
  }

  /* Introspection */

  var introspectionQuery: String
    get() = state.introspectionQuery
    set(introspectionQuery) {
      state.introspectionQuery = introspectionQuery
    }

  var isEnableIntrospectionDefaultValues: Boolean
    get() = state.enableIntrospectionDefaultValues
    set(enableIntrospectionDefaultValues) {
      state.enableIntrospectionDefaultValues = enableIntrospectionDefaultValues
    }

  var isOpenEditorWithIntrospectionResult: Boolean
    get() = state.openEditorWithIntrospectionResult
    set(openEditorWithIntrospectionResult) {
      state.openEditorWithIntrospectionResult = openEditorWithIntrospectionResult
    }

  /* Frameworks */

  var isRelaySupportEnabled: Boolean
    get() = state.enableRelayModernFrameworkSupport
    set(enableRelayModernFrameworkSupport) {
      state.enableRelayModernFrameworkSupport = enableRelayModernFrameworkSupport
    }

  var isFederationSupportEnabled: Boolean
    get() = state.enableFederationSupport
    set(enableFederationSupport) {
      state.enableFederationSupport = enableFederationSupport
    }

  var isApolloKotlinSupportEnabled: Boolean
    get() = state.enableApolloKotlinSupport
    set(enableApolloKotlinSupport) {
      state.enableApolloKotlinSupport = enableApolloKotlinSupport
    }

  class GraphQLSettingsState {
    var introspectionQuery: String = ""
    var enableIntrospectionDefaultValues: Boolean = true
    var openEditorWithIntrospectionResult: Boolean = true

    var enableRelayModernFrameworkSupport: Boolean = false
    var enableFederationSupport: Boolean = false
    var enableApolloKotlinSupport: Boolean = false
  }

  companion object {
    @JvmStatic
    fun getSettings(project: Project): GraphQLSettings = project.service<GraphQLSettings>()
  }
}
