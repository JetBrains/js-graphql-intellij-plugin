package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.FieldDefinition
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.language.ObjectTypeExtensionDefinition

class GraphQLObjectTypeCompositeDefinition
  : GraphQLExtendableCompositeDefinition<ObjectTypeDefinition, ObjectTypeExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<ObjectTypeDefinition>): ObjectTypeDefinition {
    val directives = mutableListOf<Directive>()
    val fieldDefinitions = mutableMapOf<String, FieldDefinition>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
      mergeNodes(fieldDefinitions, mapNamedNodesByKey(definition.fieldDefinitions))
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .fieldDefinitions(fieldDefinitions.values.toList())
        .sourceNodes(sourceDefinitions)
    }
  }
}
