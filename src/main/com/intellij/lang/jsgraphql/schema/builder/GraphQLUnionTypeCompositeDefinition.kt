package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.types.language.Directive
import com.intellij.lang.jsgraphql.types.language.Type
import com.intellij.lang.jsgraphql.types.language.UnionTypeDefinition
import com.intellij.lang.jsgraphql.types.language.UnionTypeExtensionDefinition

class GraphQLUnionTypeCompositeDefinition
  : GraphQLExtendableCompositeDefinition<UnionTypeDefinition, UnionTypeExtensionDefinition>() {

  override fun mergeDefinitions(sourceDefinitions: List<UnionTypeDefinition>): UnionTypeDefinition {
    val directives = mutableListOf<Directive>()
    val memberTypes = mutableMapOf<String, Type<*>>()

    for (definition in sourceDefinitions) {
      directives.addAll(definition.directives)
      mergeNodes(memberTypes, mapTypeNodesByKey(definition.memberTypes))
    }

    return sourceDefinitions.first().transform { builder ->
      builder
        .directives(directives)
        .memberTypes(memberTypes.values.toList())
        .sourceNodes(sourceDefinitions)
    }
  }
}
