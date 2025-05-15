package com.intellij.lang.jsgraphql.schema.library

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLSettings.Companion.getSettings
import com.intellij.openapi.project.Project

object GraphQLBundledLibraryTypes {
  @JvmField
  var SPECIFICATION: GraphQLLibraryDescriptor = object : GraphQLLibraryDescriptor("SPECIFICATION", GraphQLBundle.message("graphql.library.built.in")) {
    override fun isEnabled(project: Project): Boolean = true
  }

  @JvmField
  var RELAY: GraphQLLibraryDescriptor = object : GraphQLLibraryDescriptor("RELAY", GraphQLBundle.message("graphql.library.relay")) {
    override fun isEnabled(project: Project): Boolean = getSettings(project).isRelaySupportEnabled
  }

  @JvmField
  var FEDERATION: GraphQLLibraryDescriptor = object : GraphQLLibraryDescriptor("FEDERATION", GraphQLBundle.message("graphql.library.federation")) {
    override fun isEnabled(project: Project): Boolean = getSettings(project).isFederationSupportEnabled
  }

  @JvmField
  var APOLLO_KOTLIN: GraphQLLibraryDescriptor = object : GraphQLLibraryDescriptor("APOLLO_KOTLIN", GraphQLBundle.message("graphql.library.apollokotlin")) {
    override fun isEnabled(project: Project): Boolean = getSettings(project).isApolloKotlinSupportEnabled
  }
}
