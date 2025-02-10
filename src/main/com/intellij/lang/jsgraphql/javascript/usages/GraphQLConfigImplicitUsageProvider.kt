package com.intellij.lang.jsgraphql.javascript.usages

import com.intellij.lang.javascript.inspections.JSConfigImplicitUsageProvider
import com.intellij.lang.jsgraphql.GraphQLConstants.Config.CODEGEN
import com.intellij.lang.jsgraphql.GraphQLConstants.Config.GRAPHQL
import com.intellij.lang.jsgraphql.GraphQLConstants.Config.GRAPHQLRC


class GraphQLConfigImplicitUsageProvider: JSConfigImplicitUsageProvider() {
  override val configNames: Set<String> = setOf(CODEGEN, GRAPHQL, GRAPHQLRC)
}