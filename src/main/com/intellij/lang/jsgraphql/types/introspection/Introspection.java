/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.introspection;


import com.intellij.lang.jsgraphql.schema.GraphQLKnownTypes;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.openapi.progress.ProgressManager;

import java.util.HashSet;
import java.util.Set;

import static com.intellij.lang.jsgraphql.types.Scalars.GraphQLBoolean;
import static com.intellij.lang.jsgraphql.types.Scalars.GraphQLString;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLArgument.newArgument;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition.newFieldDefinition;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLList.list;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLNonNull.nonNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType.newObject;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeReference.typeRef;

@PublicApi
public final class Introspection {
  private static final Set<GraphQLNamedType> introspectionTypes = new HashSet<>();

  public enum TypeKind {
    SCALAR,
    OBJECT,
    INTERFACE,
    UNION,
    ENUM,
    INPUT_OBJECT,
    LIST,
    NON_NULL
  }

  public static final GraphQLEnumType __TypeKind = GraphQLEnumType.newEnum()
    .name("__TypeKind")
    .description("An enum describing what kind of type a given __Type is")
    .value("SCALAR", TypeKind.SCALAR, "Indicates this type is a scalar. 'specifiedByUrl' is a valid field")
    .value("OBJECT", TypeKind.OBJECT, "Indicates this type is an object. `fields` and `interfaces` are valid fields.")
    .value("INTERFACE", TypeKind.INTERFACE, "Indicates this type is an interface. `fields` and `possibleTypes` are valid fields.")
    .value("UNION", TypeKind.UNION, "Indicates this type is a union. `possibleTypes` is a valid field.")
    .value("ENUM", TypeKind.ENUM, "Indicates this type is an enum. `enumValues` is a valid field.")
    .value("INPUT_OBJECT", TypeKind.INPUT_OBJECT, "Indicates this type is an input object. `inputFields` is a valid field.")
    .value("LIST", TypeKind.LIST, "Indicates this type is a list. `ofType` is a valid field.")
    .value("NON_NULL", TypeKind.NON_NULL, "Indicates this type is a non-null. `ofType` is a valid field.")
    .build();

  public static final GraphQLObjectType __InputValue = newObject()
    .name("__InputValue")
    .field(newFieldDefinition()
             .name("name")
             .type(nonNull(GraphQLString)))
    .field(newFieldDefinition()
             .name("description")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("type")
             .type(nonNull(typeRef("__Type"))))
    .field(newFieldDefinition()
             .name("defaultValue")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("isDeprecated")
             .type(GraphQLBoolean))
    .field(newFieldDefinition()
             .name("deprecationReason")
             .type(GraphQLString))
    .build();


  public static final GraphQLObjectType __Field = newObject()
    .name("__Field")
    .field(newFieldDefinition()
             .name("name")
             .type(nonNull(GraphQLString)))
    .field(newFieldDefinition()
             .name("description")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("args")
             .type(nonNull(list(nonNull(__InputValue))))
             .argument(newArgument()
                         .name("includeDeprecated")
                         .type(GraphQLBoolean)
                         .defaultValue(false)))
    .field(newFieldDefinition()
             .name("type")
             .type(nonNull(typeRef("__Type"))))
    .field(newFieldDefinition()
             .name("isDeprecated")
             .type(nonNull(GraphQLBoolean)))
    .field(newFieldDefinition()
             .name("deprecationReason")
             .type(GraphQLString))
    .build();

  public static final GraphQLObjectType __EnumValue = newObject()
    .name("__EnumValue")
    .field(newFieldDefinition()
             .name("name")
             .type(nonNull(GraphQLString)))
    .field(newFieldDefinition()
             .name("description")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("isDeprecated")
             .type(nonNull(GraphQLBoolean)))
    .field(newFieldDefinition()
             .name("deprecationReason")
             .type(GraphQLString))
    .build();

  public static final GraphQLObjectType __Type = newObject()
    .name("__Type")
    .field(newFieldDefinition()
             .name("kind")
             .type(nonNull(__TypeKind)))
    .field(newFieldDefinition()
             .name("name")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("description")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("fields")
             .type(list(nonNull(__Field)))
             .argument(newArgument()
                         .name("includeDeprecated")
                         .type(GraphQLBoolean)
                         .defaultValue(false)))
    .field(newFieldDefinition()
             .name("interfaces")
             .type(list(nonNull(typeRef("__Type")))))
    .field(newFieldDefinition()
             .name("possibleTypes")
             .type(list(nonNull(typeRef("__Type")))))
    .field(newFieldDefinition()
             .name("enumValues")
             .type(list(nonNull(__EnumValue)))
             .argument(newArgument()
                         .name("includeDeprecated")
                         .type(GraphQLBoolean)
                         .defaultValue(false)))
    .field(newFieldDefinition()
             .name("inputFields")
             .type(list(nonNull(__InputValue)))
             .argument(newArgument()
                         .name("includeDeprecated")
                         .type(GraphQLBoolean)
                         .defaultValue(false)))
    .field(newFieldDefinition()
             .name("ofType")
             .type(typeRef("__Type")))
    .field(newFieldDefinition()
             .name("specifiedByUrl")
             .type(GraphQLString))
    .build();


  public enum DirectiveLocation {
    QUERY,
    MUTATION,
    SUBSCRIPTION,
    FIELD,
    FRAGMENT_DEFINITION,
    FRAGMENT_SPREAD,
    INLINE_FRAGMENT,
    VARIABLE_DEFINITION,
    //
    // schema SDL places
    //
    SCHEMA,
    SCALAR,
    OBJECT,
    FIELD_DEFINITION,
    ARGUMENT_DEFINITION,
    INTERFACE,
    UNION,
    ENUM,
    ENUM_VALUE,
    INPUT_OBJECT,
    INPUT_FIELD_DEFINITION
  }

  public static final GraphQLEnumType __DirectiveLocation = GraphQLEnumType.newEnum()
    .name("__DirectiveLocation")
    .description("An enum describing valid locations where a directive can be placed")
    .value("QUERY", DirectiveLocation.QUERY, "Indicates the directive is valid on queries.")
    .value("MUTATION", DirectiveLocation.MUTATION, "Indicates the directive is valid on mutations.")
    .value("SUBSCRIPTION", DirectiveLocation.SUBSCRIPTION, "Indicates the directive is valid on subscriptions.")
    .value("FIELD", DirectiveLocation.FIELD, "Indicates the directive is valid on fields.")
    .value("FRAGMENT_DEFINITION", DirectiveLocation.FRAGMENT_DEFINITION, "Indicates the directive is valid on fragment definitions.")
    .value("FRAGMENT_SPREAD", DirectiveLocation.FRAGMENT_SPREAD, "Indicates the directive is valid on fragment spreads.")
    .value("INLINE_FRAGMENT", DirectiveLocation.INLINE_FRAGMENT, "Indicates the directive is valid on inline fragments.")
    .value("VARIABLE_DEFINITION", DirectiveLocation.INPUT_FIELD_DEFINITION, "Indicates the directive is valid on variable definitions.")
    //
    // from schema SDL PR  https://github.com/facebook/graphql/pull/90
    //
    .value("SCHEMA", DirectiveLocation.SCHEMA, "Indicates the directive is valid on a schema SDL definition.")
    .value("SCALAR", DirectiveLocation.SCALAR, "Indicates the directive is valid on a scalar SDL definition.")
    .value("OBJECT", DirectiveLocation.OBJECT, "Indicates the directive is valid on an object SDL definition.")
    .value("FIELD_DEFINITION", DirectiveLocation.FIELD_DEFINITION, "Indicates the directive is valid on a field SDL definition.")
    .value("ARGUMENT_DEFINITION", DirectiveLocation.ARGUMENT_DEFINITION,
           "Indicates the directive is valid on a field argument SDL definition.")
    .value("INTERFACE", DirectiveLocation.INTERFACE, "Indicates the directive is valid on an interface SDL definition.")
    .value("UNION", DirectiveLocation.UNION, "Indicates the directive is valid on an union SDL definition.")
    .value("ENUM", DirectiveLocation.ENUM, "Indicates the directive is valid on an enum SDL definition.")
    .value("ENUM_VALUE", DirectiveLocation.ENUM_VALUE, "Indicates the directive is valid on an enum value SDL definition.")
    .value("INPUT_OBJECT", DirectiveLocation.INPUT_OBJECT, "Indicates the directive is valid on an input object SDL definition.")
    .value("INPUT_FIELD_DEFINITION", DirectiveLocation.INPUT_FIELD_DEFINITION,
           "Indicates the directive is valid on an input object field SDL definition.")
    .build();

  public static final GraphQLObjectType __Directive = newObject()
    .name("__Directive")
    .field(newFieldDefinition()
             .name("name")
             .description("The __Directive type represents a Directive that a server supports.")
             .type(nonNull(GraphQLString)))
    .field(newFieldDefinition()
             .name("description")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("isRepeatable")
             .type(nonNull(GraphQLBoolean)))
    .field(newFieldDefinition()
             .name("locations")
             .type(nonNull(list(nonNull(__DirectiveLocation)))))
    .field(newFieldDefinition()
             .name("args")
             .type(nonNull(list(nonNull(__InputValue)))))
    .field(newFieldDefinition()
             .name("onOperation")
             .type(GraphQLBoolean)
             .deprecate("Use `locations`."))
    .field(newFieldDefinition()
             .name("onFragment")
             .type(GraphQLBoolean)
             .deprecate("Use `locations`."))
    .field(newFieldDefinition()
             .name("onField")
             .type(GraphQLBoolean)
             .deprecate("Use `locations`."))
    .build();


  public static final GraphQLObjectType __Schema = newObject()
    .name("__Schema")
    .description("A GraphQL Introspection defines the capabilities" +
                 " of a GraphQL server. It exposes all available types and directives on " +
                 "the server, the entry points for query, mutation, and subscription operations.")
    .field(newFieldDefinition()
             .name("description")
             .type(GraphQLString))
    .field(newFieldDefinition()
             .name("types")
             .description("A list of all types supported by this server.")
             .type(nonNull(list(nonNull(__Type)))))
    .field(newFieldDefinition()
             .name("queryType")
             .description("The type that query operations will be rooted at.")
             .type(nonNull(__Type)))
    .field(newFieldDefinition()
             .name("mutationType")
             .description("If this server supports mutation, the type that mutation operations will be rooted at.")
             .type(__Type))
    .field(newFieldDefinition()
             .name("directives")
             .description("'A list of all directives supported by this server.")
             .type(nonNull(list(nonNull(__Directive)))))
    .field(newFieldDefinition()
             .name("subscriptionType")
             .description("'If this server support subscription, the type that subscription operations will be rooted at.")
             .type(__Type))
    .build();


  public static final GraphQLFieldDefinition SchemaMetaFieldDef = newFieldDefinition()
    .name("__schema")
    .type(nonNull(__Schema))
    .description("Access the current type schema of this server.")
    .build();

  public static final GraphQLFieldDefinition TypeMetaFieldDef = newFieldDefinition()
    .name("__type")
    .type(__Type)
    .description("Request the type information of a single type.")
    .argument(newArgument()
                .name("name")
                .type(nonNull(GraphQLString)))
    .build();

  public static final GraphQLFieldDefinition TypeNameMetaFieldDef = newFieldDefinition()
    .name("__typename")
    .type(nonNull(GraphQLString))
    .description("The name of the current Object type at runtime.")
    .build();


  private static final GraphQLObjectType IntrospectionQuery = newObject()
    .name("IntrospectionQuery")
    .field(SchemaMetaFieldDef)
    .field(TypeMetaFieldDef)
    .field(TypeNameMetaFieldDef)
    .build();

  static {
    introspectionTypes.add(__DirectiveLocation);
    introspectionTypes.add(__TypeKind);
    introspectionTypes.add(__Type);
    introspectionTypes.add(__Schema);
    introspectionTypes.add(__InputValue);
    introspectionTypes.add(__Field);
    introspectionTypes.add(__EnumValue);
    introspectionTypes.add(__Directive);
    introspectionTypes.add(IntrospectionQuery);

    // make sure all TypeReferences are resolved.
    // note: it is important to put this on the bottom of static code block.
    ProgressManager.getInstance().executeNonCancelableSection(() -> {
      GraphQLSchema.newSchema().query(IntrospectionQuery).build();
    });
  }

  public static boolean isIntrospectionTypes(GraphQLNamedType type) {
    return introspectionTypes.contains(type) || GraphQLKnownTypes.isIntrospectionType(type.getName());
  }
}
