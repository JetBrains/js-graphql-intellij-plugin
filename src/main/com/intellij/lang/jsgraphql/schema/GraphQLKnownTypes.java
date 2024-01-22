package com.intellij.lang.jsgraphql.schema;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class GraphQLKnownTypes {
  public static final String INTROSPECTION_QUERY_INTROSPECTION_META = "__QueryIntrospectionMeta";
  public static final String INTROSPECTION_TYPE_NAME_META = "__TypeNameMeta";
  public static final String INTROSPECTION_SCHEMA = "__Schema";
  public static final String INTROSPECTION_TYPE = "__Type";
  public static final String INTROSPECTION_FIELD = "__Field";
  public static final String INTROSPECTION_INPUT_VALUE = "__InputValue";
  public static final String INTROSPECTION_ENUM_VALUE = "__EnumValue";
  public static final String INTROSPECTION_TYPE_KIND = "__TypeKind";
  public static final String INTROSPECTION_DIRECTIVE = "__Directive";
  public static final String INTROSPECTION_DIRECTIVE_LOCATION = "__DirectiveLocation";

  public static final String INTROSPECTION_TYPENAME_FIELD = "__typename";

  public static final Set<String> INTROSPECTION_TYPES = Set.of(
    INTROSPECTION_QUERY_INTROSPECTION_META,
    INTROSPECTION_TYPE_NAME_META,
    INTROSPECTION_SCHEMA,
    INTROSPECTION_TYPE,
    INTROSPECTION_FIELD,
    INTROSPECTION_INPUT_VALUE,
    INTROSPECTION_ENUM_VALUE,
    INTROSPECTION_TYPE_KIND,
    INTROSPECTION_DIRECTIVE,
    INTROSPECTION_DIRECTIVE_LOCATION
  );

  public static final String DIRECTIVE_DEPRECATED = "deprecated";
  public static final String DIRECTIVE_DEPRECATED_REASON = "reason";
  public static final String DIRECTIVE_SKIP = "skip";
  public static final String DIRECTIVE_INCLUDE = "include";
  public static final String DIRECTIVE_SPECIFIED_BY = "specifiedBy";

  public static final Set<String> DEFAULT_DIRECTIVES =
    Set.of(DIRECTIVE_DEPRECATED, DIRECTIVE_SKIP, DIRECTIVE_INCLUDE, DIRECTIVE_SPECIFIED_BY);

  public static final String QUERY_TYPE = "Query";
  public static final String MUTATION_TYPE = "Mutation";
  public static final String SUBSCRIPTION_TYPE = "Subscription";

  public static final String ID_TYPE = "ID";
  public static final String BOOLEAN_TYPE = "Boolean";
  public static final String STRING_TYPE = "String";
  public static final String INT_TYPE = "Int";
  public static final String FLOAT_TYPE = "Float";

  public static boolean isIntrospectionType(@Nullable String name) {
    return INTROSPECTION_TYPES.contains(name);
  }
}
