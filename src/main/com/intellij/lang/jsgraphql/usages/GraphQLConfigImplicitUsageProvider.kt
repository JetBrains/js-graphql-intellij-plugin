package com.intellij.lang.jsgraphql.usages

import com.intellij.lang.javascript.inspections.JSConfigImplicitUsageProvider
import com.intellij.lang.jsgraphql.GraphQLConstants.Config.CODEGEN
import com.intellij.lang.jsgraphql.GraphQLConstants.Config.GRAPHQL


class GraphQLConfigImplicitUsageProvider: JSConfigImplicitUsageProvider() {
  override val configNames = setOf(CODEGEN, GRAPHQL)
}