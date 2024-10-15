package com.intellij.lang.jsgraphql.schema.builder

import com.intellij.lang.jsgraphql.schema.isExtensionDefinition
import com.intellij.lang.jsgraphql.types.GraphQLError
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.types.language.*
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaExtensionsChecker
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.lang.jsgraphql.types.schema.idl.errors.DirectiveRedefinitionError
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaRedefinitionError
import com.intellij.lang.jsgraphql.types.schema.idl.errors.TypeRedefinitionError
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager

internal class GraphQLCompositeRegistry {

  private val namedCompositeDefinitions = mutableMapOf<String, GraphQLCompositeDefinition<*>>()
  private val schemaCompositeDefinition = GraphQLSchemaTypeCompositeDefinition()

  @Throws(GraphQLException::class)
  fun merge(source: TypeDefinitionRegistry) {
    if (source.schemaDefinition().isPresent) {
      addTypeDefinition(source.schemaDefinition().get())
    }

    source.types().values.forEach(::addTypeDefinition)
    source.directiveDefinitions.values.forEach(::addTypeDefinition)
    source.scalars().values.forEach(::addTypeDefinition)

    source.schemaExtensionDefinitions.forEach(::addExtensionDefinition)

    sequenceOf(
      source.objectTypeExtensions(),
      source.interfaceTypeExtensions(),
      source.unionTypeExtensions(),
      source.enumTypeExtensions(),
      source.scalarTypeExtensions(),
      source.inputObjectTypeExtensions()
    )
      .flatMap { it.values.asSequence() }
      .flatten()
      .forEach {
        addExtensionDefinition(it)
      }
  }

  private fun getCompositeDefinition(definition: SDLDefinition<*>): GraphQLCompositeDefinition<*>? {
    if (definition is SchemaDefinition) {
      return schemaCompositeDefinition
    }
    if (definition !is NamedNode<*>) {
      return null
    }
    val name = (definition as NamedNode<*>).name
    return if (name.isEmpty()) null
    else namedCompositeDefinitions.computeIfAbsent(name) {
      createCompositeDefinition(definition)
    }
  }

  private fun addTypeDefinition(definition: SDLDefinition<*>) {
    LOG.assertTrue(!isExtensionDefinition(definition))

    val builder = getCompositeDefinition(definition) ?: return
    when (builder) {
      is GraphQLDirectiveTypeCompositeDefinition -> builder.addDefinition(definition as? DirectiveDefinition)
      is GraphQLEnumTypeCompositeDefinition -> builder.addDefinition(definition as? EnumTypeDefinition)
      is GraphQLInputObjectTypeCompositeDefinition -> builder.addDefinition(definition as? InputObjectTypeDefinition)
      is GraphQLInterfaceTypeCompositeDefinition -> builder.addDefinition(definition as? InterfaceTypeDefinition)
      is GraphQLObjectTypeCompositeDefinition -> builder.addDefinition(definition as? ObjectTypeDefinition)
      is GraphQLScalarTypeCompositeDefinition -> builder.addDefinition(definition as? ScalarTypeDefinition)
      is GraphQLSchemaTypeCompositeDefinition -> builder.addDefinition(definition as? SchemaDefinition)
      is GraphQLUnionTypeCompositeDefinition -> builder.addDefinition(definition as? UnionTypeDefinition)
      else -> LOG.error("Unknown builder type: " + builder.javaClass.name)
    }
  }

  private fun addExtensionDefinition(definition: SDLDefinition<*>) {
    LOG.assertTrue(isExtensionDefinition(definition))

    val builder =
      getCompositeDefinition(definition) as? GraphQLExtendableCompositeDefinition<*, *> ?: return

    when (builder) {
      is GraphQLEnumTypeCompositeDefinition -> builder.addExtension(definition as? EnumTypeExtensionDefinition)
      is GraphQLInputObjectTypeCompositeDefinition -> builder.addExtension(definition as? InputObjectTypeExtensionDefinition)
      is GraphQLInterfaceTypeCompositeDefinition -> builder.addExtension(definition as? InterfaceTypeExtensionDefinition)
      is GraphQLObjectTypeCompositeDefinition -> builder.addExtension(definition as? ObjectTypeExtensionDefinition)
      is GraphQLScalarTypeCompositeDefinition -> builder.addExtension(definition as? ScalarTypeExtensionDefinition)
      is GraphQLSchemaTypeCompositeDefinition -> builder.addExtension(definition as? SchemaExtensionDefinition)
      is GraphQLUnionTypeCompositeDefinition -> builder.addExtension(definition as? UnionTypeExtensionDefinition)
      else -> LOG.error("Unknown extension builder type: " + builder.javaClass.name)
    }
  }

  private fun addDefinition(definition: SDLDefinition<*>) {
    ProgressManager.checkCanceled()

    if (isExtensionDefinition(definition)) {
      addExtensionDefinition(definition)
    }
    else {
      addTypeDefinition(definition)
    }
  }

  fun addFromDocument(document: Document) {
    val definitions = document.definitions
    for (definition in definitions) {
      if (definition is SDLDefinition<*>) {
        addDefinition(definition)
      }
    }
  }

  fun build(): TypeDefinitionRegistry {
    val registry = TypeDefinitionRegistry()

    val schemaDefinition = schemaCompositeDefinition.buildDefinition()
    if (schemaDefinition != null) {
      registry.add(schemaDefinition)
    }
    schemaCompositeDefinition.sourceExtensions.forEach(registry::add)

    namedCompositeDefinitions.values.forEach { builder: GraphQLCompositeDefinition<*> ->
      val definition = builder.buildDefinition()
      if (definition != null) {
        registry.add(definition)
      }
      if (builder is GraphQLExtendableCompositeDefinition<*, *>) {
        builder.sourceExtensions.forEach(registry::add)
      }
    }

    for (additionalError in validate()) {
      registry.addError(additionalError)
    }

    return registry
  }

  private fun validate(): List<GraphQLException> {
    val errors = ArrayList<GraphQLException>()

    val sourceSchemaDefinitions = schemaCompositeDefinition.sourceDefinitions
    if (sourceSchemaDefinitions.size > 1) {
      for (schemaDefinition in sourceSchemaDefinitions.drop(1)) {
        errors.add(SchemaRedefinitionError(schemaDefinition))
      }
    }

    val operationRedefinitionErrors: MutableList<GraphQLError> = mutableListOf()
    val operationDefs: MutableMap<String, OperationTypeDefinition> = mutableMapOf()
    for (sourceSchemaDefinition in sourceSchemaDefinitions) {
      // pass an empty list because extensions are validated in a separate extensions validator
      SchemaExtensionsChecker.gatherOperationDefs(
        operationDefs,
        operationRedefinitionErrors,
        sourceSchemaDefinition,
        emptyList()
      )
    }
    if (operationRedefinitionErrors.isNotEmpty()) {
      errors.add(SchemaProblem(operationRedefinitionErrors))
    }

    namedCompositeDefinitions.values.forEach { compositeDefinition: GraphQLCompositeDefinition<*> ->
      if (compositeDefinition is GraphQLDirectiveTypeCompositeDefinition) {
        val sourceDefinitions = compositeDefinition.sourceDefinitions
        if (sourceDefinitions.size > 1) {
          val initialDefinition = sourceDefinitions[0]
          for (i in 1 until sourceDefinitions.size) {
            errors.add(DirectiveRedefinitionError(sourceDefinitions[i], initialDefinition))
          }
        }
        return@forEach
      }

      val sourceDefinitions = compositeDefinition.sourceDefinitions.filterIsInstance<TypeDefinition<*>>()
      if (sourceDefinitions.size > 1) {
        val initialDefinition = sourceDefinitions[0]
        for (i in 1 until sourceDefinitions.size) {
          errors.add(TypeRedefinitionError(sourceDefinitions[i], initialDefinition))
        }
      }
    }

    return errors
  }

  companion object {
    private val LOG: Logger = logger<GraphQLCompositeRegistry>()

    private fun createCompositeDefinition(definition: SDLDefinition<*>): GraphQLCompositeDefinition<*> {
      return when (definition) {
        is InputObjectTypeDefinition -> GraphQLInputObjectTypeCompositeDefinition()
        is ObjectTypeDefinition -> GraphQLObjectTypeCompositeDefinition()
        is InterfaceTypeDefinition -> GraphQLInterfaceTypeCompositeDefinition()
        is UnionTypeDefinition -> GraphQLUnionTypeCompositeDefinition()
        is EnumTypeDefinition -> GraphQLEnumTypeCompositeDefinition()
        is ScalarTypeDefinition -> GraphQLScalarTypeCompositeDefinition()
        is DirectiveDefinition -> GraphQLDirectiveTypeCompositeDefinition()
        is SchemaDefinition -> GraphQLSchemaTypeCompositeDefinition()
        else -> throw IllegalStateException("Unknown definition type: " + definition.javaClass.name)
      }
    }
  }
}
