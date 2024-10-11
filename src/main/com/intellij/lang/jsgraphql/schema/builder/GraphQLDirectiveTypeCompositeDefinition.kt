package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.DirectiveDefinition

class GraphQLDirectiveTypeCompositeDefinition : GraphQLCompositeDefinition<DirectiveDefinition>() {
  override fun mergeDefinitions(sourceDefinitions: List<DirectiveDefinition>): DirectiveDefinition {
    return sourceDefinitions.first().transform { builder -> builder.sourceNodes(sourceDefinitions) }
  }
}
