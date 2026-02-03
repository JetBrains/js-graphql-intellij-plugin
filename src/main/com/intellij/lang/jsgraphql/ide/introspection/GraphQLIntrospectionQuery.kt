@file:JvmName("GraphQLIntrospectionQuery")

package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.DIRECTIVE_IS_REPEATABLE
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.INCLUDE_DEPRECATED_DIRECTIVE_ARGS
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.INCLUDE_DEPRECATED_FIELD_ARGS
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.INCLUDE_DEPRECATED_INPUT_FIELDS
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.INPUT_VALUE_DEFAULT_VALUE
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.INPUT_VALUE_DEPRECATION_REASON
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.INPUT_VALUE_IS_DEPRECATED
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLSchemaCapability.SPECIFIED_BY_URL
import java.util.EnumSet

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
    ${includeIf(capabilities, SPECIFIED_BY_URL, "specifiedByURL")}
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
 * [SPECIFIED_BY_URL]                  | __Type > fields[] > "name": "specifiedByURL"
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
