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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;

/**
 * A utility class that helps work with {@link com.intellij.lang.jsgraphql.types.schema.GraphQLType}s
 */
@PublicApi
public class GraphQLTypeUtil {

  /**
   * This will return the type in graphql SDL format, eg [typeName!]!
   *
   * @param type the type in play
   * @return the type in graphql SDL format, eg [typeName!]!
   */
  public static @NotNull String simplePrint(GraphQLType type) {
    if (type == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    if (isNonNull(type)) {
      sb.append(simplePrint(unwrapOne(type)));
      sb.append("!");
    }
    else if (isList(type)) {
      sb.append("[");
      sb.append(simplePrint(unwrapOne(type)));
      sb.append("]");
    }
    else {
      sb.append(((GraphQLNamedType)type).getName());
    }
    return sb.toString();
  }

  public static String simplePrint(GraphQLSchemaElement schemaElement) {
    if (schemaElement instanceof GraphQLType) {
      return simplePrint((GraphQLType)schemaElement);
    }
    if (schemaElement instanceof GraphQLNamedSchemaElement) {
      return ((GraphQLNamedSchemaElement)schemaElement).getName();
    }
    // a schema element is either a GraphQLType or a GraphQLNamedSchemaElement
    return assertShouldNeverHappen("unexpected schema element: " + schemaElement);
  }

  /**
   * Returns true if the given type is a non null type
   *
   * @param type the type to check
   * @return true if the given type is a non null type
   */
  public static boolean isNonNull(GraphQLType type) {
    return type instanceof GraphQLNonNull;
  }

  /**
   * Returns true if the given type is a nullable type
   *
   * @param type the type to check
   * @return true if the given type is a nullable type
   */
  public static boolean isNullable(GraphQLType type) {
    return !isNonNull(type);
  }

  /**
   * Returns true if the given type is a list type
   *
   * @param type the type to check
   * @return true if the given type is a list type
   */
  public static boolean isList(GraphQLType type) {
    return type instanceof GraphQLList;
  }

  /**
   * Returns true if the given type is a non null or list type, that is a wrapped type
   *
   * @param type the type to check
   * @return true if the given type is a non null or list type
   */
  public static boolean isWrapped(GraphQLType type) {
    return isList(type) || isNonNull(type);
  }

  /**
   * Returns true if the given type is NOT a non null or list type
   *
   * @param type the type to check
   * @return true if the given type is NOT a non null or list type
   */
  public static boolean isNotWrapped(GraphQLType type) {
    return !isWrapped(type);
  }

  /**
   * Returns true if the given type is a scalar type
   *
   * @param type the type to check
   * @return true if the given type is a scalar type
   */
  public static boolean isScalar(GraphQLType type) {
    return type instanceof GraphQLScalarType;
  }

  /**
   * Returns true if the given type is an enum type
   *
   * @param type the type to check
   * @return true if the given type is an enum type
   */
  public static boolean isEnum(GraphQLType type) {
    return type instanceof GraphQLEnumType;
  }

  /**
   * Returns true if the given type is a leaf type, that it cant contain any more fields
   *
   * @param type the type to check
   * @return true if the given type is a leaf type
   */
  public static boolean isLeaf(GraphQLType type) {
    GraphQLUnmodifiedType unmodifiedType = unwrapAll(type);
    return
      unmodifiedType instanceof GraphQLScalarType
      || unmodifiedType instanceof GraphQLEnumType;
  }

  /**
   * Returns true if the given type is an input type
   *
   * @param type the type to check
   * @return true if the given type is an input type
   */
  public static boolean isInput(GraphQLType type) {
    GraphQLUnmodifiedType unmodifiedType = unwrapAll(type);
    return
      unmodifiedType instanceof GraphQLScalarType
      || unmodifiedType instanceof GraphQLEnumType
      || unmodifiedType instanceof GraphQLInputObjectType;
  }

  /**
   * Unwraps one layer of the type or just returns the type again if its not a wrapped type
   *
   * @param type the type to unwrapOne
   * @return the unwrapped type or the same type again if its not wrapped
   */
  public static GraphQLType unwrapOne(GraphQLType type) {
    if (isNonNull(type)) {
      return ((GraphQLNonNull)type).getWrappedType();
    }
    else if (isList(type)) {
      return ((GraphQLList)type).getWrappedType();
    }
    return type;
  }

  /**
   * Unwraps all layers of the type or just returns the type again if its not a wrapped type
   *
   * @param type the type to unwrapOne
   * @return the underlying type
   */
  public static GraphQLUnmodifiedType unwrapAll(GraphQLType type) {
    while (true) {
      if (isNotWrapped(type)) {
        return (GraphQLUnmodifiedType)type;
      }
      type = unwrapOne(type);
    }
  }

  public static GraphQLType unwrapNonNull(GraphQLType type) {
    while (isNonNull(type)) {
      type = unwrapOne(type);
    }
    return type;
  }

  /**
   * graphql types can be wrapped in {@link GraphQLNonNull} and {@link GraphQLList} type wrappers
   * so this method will unwrap the type down to the raw unwrapped type and return that wrapping
   * as a stack, with the top of the stack being the raw underling type.
   *
   * @param type the type to unwrap
   * @return a stack of the type wrapping which will be at least 1 later deep
   */
  public static Stack<GraphQLType> unwrapType(GraphQLType type) {
    type = assertNotNull(type);
    Stack<GraphQLType> decoration = new Stack<>();
    while (true) {
      decoration.push(type);
      if (isNotWrapped(type)) {
        break;
      }
      type = unwrapOne(type);
    }
    return decoration;
  }
}
