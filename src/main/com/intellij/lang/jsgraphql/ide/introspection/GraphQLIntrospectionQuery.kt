@file:JvmName("GraphQLIntrospectionQuery")

package com.intellij.lang.jsgraphql.ide.introspection

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.*
import java.util.*

private const val INCLUDE_DEPRECATED_PARAMS = "(includeDeprecated: true)"

private fun includeDeprecated(capabilities: EnumSet<GraphQLSchemaCapability>, desiredCapability: GraphQLSchemaCapability) =
  includeIf(capabilities, desiredCapability, INCLUDE_DEPRECATED_PARAMS)

private fun includeIf(capabilities: EnumSet<GraphQLSchemaCapability>, desiredCapability: GraphQLSchemaCapability, text: String) =
  if (capabilities.contains(desiredCapability)) text else ""

fun buildIntrospectionQueryFromTemplate(capabilities: EnumSet<GraphQLSchemaCapability>): String = """
  query IntrospectionQuery {
    __schema {
      queryType { name }
      mutationType { name }
      subscriptionType { name }
      types {
        ...FullType
      }
      directives {
        name
        description
        locations
        args${includeDeprecated(capabilities, INCLUDE_DEPRECATED_DIRECTIVE_ARGS)} {
          ...InputValue
        }
        ${includeIf(capabilities, DIRECTIVE_IS_REPEATABLE, "isRepeatable")}
      }
    }
  }
  
  fragment FullType on __Type {
    kind
    name
    description
    fields(includeDeprecated: true) {
      name
      description
      args${includeDeprecated(capabilities, INCLUDE_DEPRECATED_FIELD_ARGS)} {
        ...InputValue
      }
      type {
        ...TypeRef
      }
      isDeprecated
      deprecationReason
    }
    inputFields${includeDeprecated(capabilities, INCLUDE_DEPRECATED_INPUT_FIELDS)} {
      ...InputValue
    }
    interfaces {
      ...TypeRef
    }
    enumValues(includeDeprecated: true) {
      name
      description
      isDeprecated
      deprecationReason
    }
    possibleTypes {
      ...TypeRef
    }
  }
  
  fragment InputValue on __InputValue {
    name
    description
    type { ...TypeRef }
    ${includeIf(capabilities, INPUT_VALUE_DEFAULT_VALUE, "defaultValue")}
    ${includeIf(capabilities, INPUT_VALUE_IS_DEPRECATED, "isDeprecated")}
    ${includeIf(capabilities, INPUT_VALUE_DEPRECATION_REASON, "deprecationReason")}
  }
  
  fragment TypeRef on __Type {
    kind
    name
    ofType {
      kind
      name
      ofType {
        kind
        name
        ofType {
          kind
          name
          ofType {
            kind
            name
            ofType {
              kind
              name
              ofType {
                kind
                name
                ofType {
                  kind
                  name
                }
              }
            }
          }
        }
      }
    }
  }
""".trimIndent()

/**
 * Introspection query that returns the structure of the __Type, __Field, __Directive, and __InputValue fields.
 * See [parseSchemaCapabilities].
 *
 * The following capabilities are located:
 *
 * Capability                          | Introspection query path
 * ------------------------------------|---------------------------------------
 * [INCLUDE_DEPRECATED_INPUT_FIELDS]   | __Type > fields[] > "name": "inputFields" > args[] > "name": "includeDeprecated"
 * [INCLUDE_DEPRECATED_FIELD_ARGS]     | __Field > fields[] > "name": "args" > args[] > "name": "includeDeprecated"
 * [INCLUDE_DEPRECATED_DIRECTIVE_ARGS] | __Directive > fields[] > "name": "args" > args[] > "name": "includeDeprecated"
 * [INPUT_VALUE_IS_DEPRECATED]         | __InputValue > fields[] > "name": "isDeprecated"
 * [INPUT_VALUE_DEPRECATION_REASON]    | __InputValue > fields[] > "name": "deprecationReason"
 * [INPUT_VALUE_DEFAULT_VALUE]         | __InputValue > fields[] > "name": "defaultValue"
 * [DIRECTIVE_IS_REPEATABLE]           |  __Directive > fields[] > "name": "isRepeatable"
 */
val INTROSPECTION_SCHEMA_CAPABILITIES_QUERY: String = """
  query IntrospectionCapabilitiesQuery {
    __schema {
      types {
        name
        fields {
          name
          args {
            name
          }
        }
      }
    }
  }
""".trimIndent()


internal fun parseSchemaCapabilities(introspectionResponse: JsonObject): EnumSet<GraphQLSchemaCapability> {
  val responseObject = if (introspectionResponse.has("data"))
    introspectionResponse.getAsJsonObject("data")!!
  else
    introspectionResponse

  if (introspectionResponse.has("errors")) {
    throw IllegalArgumentException(
      GraphQLBundle.message(
        "graphql.introspection.capabilities.detection.failed.errors",
        introspectionResponse.get("errors")?.toString() ?: "[]"
      )
    )
  }

  val typeDefinition = responseObject.getTypeNonNull("__Type")
  val fieldDefinition = responseObject.getTypeNonNull("__Field")
  val directiveDefinition = responseObject.getTypeNonNull("__Directive")
  val inputValueDefinition = responseObject.getTypeNonNull("__InputValue")

  val capabilities = EnumSet.noneOf(GraphQLSchemaCapability::class.java)
  if (typeDefinition.hasArgument("inputFields", "includeDeprecated")) {
    capabilities.add(INCLUDE_DEPRECATED_INPUT_FIELDS)
  }
  if (fieldDefinition.hasArgument("args", "includeDeprecated")) {
    capabilities.add(INCLUDE_DEPRECATED_FIELD_ARGS)
  }
  if (directiveDefinition.hasArgument("args", "includeDeprecated")) {
    capabilities.add(INCLUDE_DEPRECATED_DIRECTIVE_ARGS)
  }
  if (inputValueDefinition.hasField("isDeprecated")) {
    capabilities.add(INPUT_VALUE_IS_DEPRECATED)
  }
  if (inputValueDefinition.hasField("deprecationReason")) {
    capabilities.add(INPUT_VALUE_DEPRECATION_REASON)
  }
  if (inputValueDefinition.hasField("defaultValue")) {
    capabilities.add(INPUT_VALUE_DEFAULT_VALUE)
  }
  if (directiveDefinition.hasField("isRepeatable")) {
    capabilities.add(DIRECTIVE_IS_REPEATABLE)
  }
  return capabilities
}

private fun JsonElement.getTypeNonNull(typeName: String): JsonObject =
  getType(typeName) ?: throw IllegalArgumentException("Missing $typeName type definition in introspection response")

private fun JsonElement.getType(typeName: String): JsonObject? {
  if (!isJsonObject) return null
  val schema = asJsonObject.get("__schema") as? JsonObject
  val typesElement = schema?.get("types") as? JsonArray
  return typesElement?.find { it.isJsonObject && it.asJsonObject.get("name")?.asString == typeName } as? JsonObject
}

private fun JsonElement.getField(fieldName: String): JsonObject? {
  if (!isJsonObject) return null
  val fieldsElement = asJsonObject.get("fields") as? JsonArray
  return fieldsElement?.find { it.isJsonObject && it.asJsonObject.get("name")?.asString == fieldName } as? JsonObject
}

private fun JsonElement.getArgument(fieldName: String, argName: String): JsonObject? {
  val typeField = getField(fieldName)
  val argsElement = typeField?.get("args") as? JsonArray
  return argsElement?.find { it.isJsonObject && it.asJsonObject.get("name")?.asString == argName } as? JsonObject
}

private fun JsonElement.hasField(fieldName: String): Boolean =
  getField(fieldName) != null

private fun JsonElement.hasArgument(fieldName: String, argName: String): Boolean =
  getArgument(fieldName, argName) != null
