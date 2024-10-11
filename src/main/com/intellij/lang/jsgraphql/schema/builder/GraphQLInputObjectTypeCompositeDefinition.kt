package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.language.InputObjectTypeExtensionDefinition
import com.intellij.lang.jsgraphql.types.language.InputValueDefinition

class GraphQLInputObjectTypeCompositeDefinition
  : GraphQLExtendableCompositeDefinition<InputObjectTypeDefinition, InputObjectTypeExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<InputObjectTypeDefinition>): InputObjectTypeDefinition {
    val directives = mutableListOf<Directive>()
    val inputValueDefinitions = mutableMapOf<String, InputValueDefinition>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
      mergeNodes(inputValueDefinitions, mapNamedNodesByKey(definition.inputValueDefinitions))
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .inputValueDefinitions(inputValueDefinitions.values.toList())
        .sourceNodes(sourceDefinitions)
    }
  }
}
