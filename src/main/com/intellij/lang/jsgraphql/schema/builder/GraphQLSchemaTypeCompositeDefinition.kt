package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.OperationTypeDefinition
import com.intellij.lang.jsgraphql.types.language.SchemaDefinition
import com.intellij.lang.jsgraphql.types.language.SchemaExtensionDefinition

class GraphQLSchemaTypeCompositeDefinition
  : GraphQLExtendableCompositeDefinition<SchemaDefinition, SchemaExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<SchemaDefinition>): SchemaDefinition {
    val directives = mutableListOf<Directive>()
    val operationTypeDefinitions = mutableMapOf<String, OperationTypeDefinition>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
      mergeNodes(operationTypeDefinitions, mapNamedNodesByKey(definition.operationTypeDefinitions))
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .operationTypeDefinitions(operationTypeDefinitions.values.toList())
        .sourceNodes(sourceDefinitions)
    }
  }
}
