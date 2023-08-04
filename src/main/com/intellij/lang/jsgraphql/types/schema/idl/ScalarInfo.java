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
package com.intellij.lang.jsgraphql.types.schema.idl;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.Scalars;
import com.intellij.lang.jsgraphql.types.language.ScalarTypeDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Info on all the standard scalar objects provided by graphql-java
 */
@PublicApi
public class ScalarInfo {

  /**
   * A list of the built-in scalar types as defined by the graphql specification
   */
  public static final List<GraphQLScalarType> GRAPHQL_SPECIFICATION_SCALARS = new ArrayList<>();

  public static final Map<String, GraphQLScalarType> GRAPHQL_SPECIFICATION_SCALARS_MAP = new LinkedHashMap<>();

  /**
   * A map of scalar type definitions provided by graphql-java
   */
  public static final Map<String, ScalarTypeDefinition> GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS = new LinkedHashMap<>();

  static {
    GRAPHQL_SPECIFICATION_SCALARS.add(Scalars.GraphQLInt);
    GRAPHQL_SPECIFICATION_SCALARS.add(Scalars.GraphQLFloat);
    GRAPHQL_SPECIFICATION_SCALARS.add(Scalars.GraphQLString);
    GRAPHQL_SPECIFICATION_SCALARS.add(Scalars.GraphQLBoolean);
    GRAPHQL_SPECIFICATION_SCALARS.add(Scalars.GraphQLID);

    GRAPHQL_SPECIFICATION_SCALARS.forEach(scalar -> GRAPHQL_SPECIFICATION_SCALARS_MAP.put(scalar.getName(), scalar));
  }

  static {
    // graphql standard scalars
    GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.put("Int", ScalarTypeDefinition.newScalarTypeDefinition().name("Int").build());
    GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.put("Float", ScalarTypeDefinition.newScalarTypeDefinition().name("Float").build());
    GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.put("String", ScalarTypeDefinition.newScalarTypeDefinition().name("String").build());
    GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.put("Boolean", ScalarTypeDefinition.newScalarTypeDefinition().name("Boolean").build());
    GRAPHQL_SPECIFICATION_SCALARS_DEFINITIONS.put("ID", ScalarTypeDefinition.newScalarTypeDefinition().name("ID").build());
  }

  /**
   * Returns true if the scalar type is a scalar that is specified by the graphql specification
   *
   * @param scalarTypeName the name of the scalar type in question
   * @return true if the scalar type is is specified by the graphql specification
   */
  public static boolean isGraphqlSpecifiedScalar(String scalarTypeName) {
    return inList(GRAPHQL_SPECIFICATION_SCALARS, scalarTypeName);
  }

  /**
   * Returns true if the scalar type is a scalar that is specified by the graphql specification
   *
   * @param scalarType the type in question
   * @return true if the scalar type is is specified by the graphql specification
   */
  public static boolean isGraphqlSpecifiedScalar(GraphQLScalarType scalarType) {
    return inList(GRAPHQL_SPECIFICATION_SCALARS, scalarType.getName());
  }

  private static boolean inList(List<GraphQLScalarType> scalarList, String scalarTypeName) {
    return scalarList.stream().anyMatch(sc -> sc.getName().equals(scalarTypeName));
  }
}
