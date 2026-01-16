package com.intellij.lang.jsgraphql.ide.introspection

enum class GraphQLSchemaCapability {
  /**
   * For example, the following GraphQL schema:
   *
   * ```graphql
   * input InputType {
   *     someInputField: String @deprecated
   * }
   * ```
   *
   * The introspection schema is defined like this:
   * ```graphql
   * type __Type {
   *     inputFields(includeDeprecated: Boolean = false): [__InputValue!]
   * }
   * ```
   */
  INCLUDE_DEPRECATED_INPUT_FIELDS,

  /**
   * For example, the following GraphQL schema:
   *
   * ```graphql
   * type Query {
   *     someField(someArg: Int @deprecated) : String
   * }
   * ```
   *
   * The introspection schema is defined like this:
   * ```graphql
   * type __Field {
   *     args(includeDeprecated: Boolean = false): [__InputValue!]!
   * }
   * ```
   */
  INCLUDE_DEPRECATED_FIELD_ARGS,

  /**
   * The introspection schema is defined like this:
   * ```graphql
   * type __Directive {
   *   args(includeDeprecated: Boolean = false): [__InputValue!]!
   * }
   * ```
   */
  INCLUDE_DEPRECATED_DIRECTIVE_ARGS,

  /**
   * The introspection schema is defined like this:
   * ```graphql
   * type __InputValue {
   *   isDeprecated: Boolean!
   * }
   * ```
   */
  INPUT_VALUE_IS_DEPRECATED,

  /**
   * The introspection schema is defined like this:
   * ```graphql
   * type __InputValue {
   *   deprecationReason: String
   * }
   * ```
   */
  INPUT_VALUE_DEPRECATION_REASON,

  /**
   * The introspection schema is defined like this:
   * ```graphql
   * type __InputValue {
   *   defaultValue: String
   * }
   * ```
   */
  INPUT_VALUE_DEFAULT_VALUE,

  /**
   * Allows adding `repeatable` modifier to directive definitions.
   *
   * For example, the following GraphQL schema:
   *
   * ```graphql
   * directive @some_directive repeatable on FIELD
   * ```
   *
   * The introspection schema is defined like this:
   * ```graphql
   * type __Directive {
   *   isRepeatable: Boolean!
   * }
   * ```
   */
  DIRECTIVE_IS_REPEATABLE,

  /**
   * Allows specifying a URL that provides the specification for a scalar type.
   *
   * For example, the following GraphQL schema:
   *
   * ```graphql
   * scalar DateTime @specifiedBy(url: "https://scalars.graphql.org/andimarek/date-time")
   * ```
   *
   * The introspection schema is defined like this:
   * ```graphql
   * type __Type {
   *   specifiedByURL: String
   * }
   * ```
   */
  SPECIFIED_BY_URL,
}