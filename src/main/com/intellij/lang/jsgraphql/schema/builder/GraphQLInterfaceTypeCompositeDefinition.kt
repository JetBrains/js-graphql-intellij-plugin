package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.FieldDefinition
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeDefinition
import com.intellij.lang.jsgraphql.types.language.InterfaceTypeExtensionDefinition

class GraphQLInterfaceTypeCompositeDefinition :
  GraphQLExtendableCompositeDefinition<InterfaceTypeDefinition, InterfaceTypeExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<InterfaceTypeDefinition>): InterfaceTypeDefinition {
    val directives = mutableListOf<Directive>()
    val fieldDefinitions = mutableMapOf<String, FieldDefinition>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
      mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.fieldDefinitions))
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .definitions(fieldDefinitions.values.toList())
        .sourceNodes(sourceDefinitions)
    }
  }
}
