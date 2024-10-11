package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.EnumTypeDefinition
import com.intellij.lang.jsgraphql.types.language.EnumTypeExtensionDefinition
import com.intellij.lang.jsgraphql.types.language.EnumValueDefinition

class GraphQLEnumTypeCompositeDefinition
  : GraphQLExtendableCompositeDefinition<EnumTypeDefinition, EnumTypeExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<EnumTypeDefinition>): EnumTypeDefinition {
    val directives = mutableListOf<Directive>()
    val enumValueDefinitions = mutableMapOf<String, EnumValueDefinition>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
      mergeNodes(enumValueDefinitions, mapNamedNodesByKey(definition.enumValueDefinitions))
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .enumValueDefinitions(enumValueDefinitions.values.toList())
        .sourceNodes(sourceDefinitions)
    }
  }
}
