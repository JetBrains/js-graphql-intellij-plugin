package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project

interface GraphQLConfigContributor {
  companion object {
    @JvmField
    val EP_NAME: ExtensionPointName<GraphQLConfigContributor> =
      ExtensionPointName.create<GraphQLConfigContributor>("com.intellij.lang.jsgraphql.configContributor")
  }

  fun contributeConfigs(project: Project): Collection<GraphQLConfig>
}
