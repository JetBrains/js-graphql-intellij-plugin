package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition
import com.intellij.lang.jsgraphql.types.language.ScalarTypeExtensionDefinition

class GraphQLScalarTypeCompositeDefinition
  : GraphQLExtendableCompositeDefinition<ScalarTypeDefinition, ScalarTypeExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<ScalarTypeDefinition>): ScalarTypeDefinition {
    val directives = mutableListOf<Directive>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .sourceNodes(sourceDefinitions)
    }
  }
}
