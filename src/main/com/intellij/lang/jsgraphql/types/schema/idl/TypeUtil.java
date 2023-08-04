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

import com.intellij.lang.jsgraphql.types.language.ListType;
import com.intellij.lang.jsgraphql.types.language.NonNullType;
import com.intellij.lang.jsgraphql.types.language.Type;
import com.intellij.lang.jsgraphql.types.language.TypeName;

/**
 * This class consists of {@code static} utility methods for operating on {@link com.intellij.lang.jsgraphql.types.language.Type}.
 */
public class TypeUtil {

  /**
   * This will return the type in graphql SDL format, eg [typeName!]!
   *
   * @param type the type in play
   * @return the type in graphql SDL format, eg [typeName!]!
   */
  public static String simplePrint(Type type) {
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
      sb.append(((TypeName)type).getName());
    }
    return sb.toString();
  }

  /**
   * Unwraps all layers of the type or just returns the type again if its not a wrapped type
   *
   * @param type the type to be unwrapped
   * @return the unwrapped type or the same type again if its not wrapped
   */
  public static TypeName unwrapAll(Type type) {
    if (isList(type)) {
      return unwrapAll(((ListType)type).getType());
    }
    else if (type instanceof NonNullType) {
      return unwrapAll(((NonNullType)type).getType());
    }
    return (TypeName)type;
  }

  /**
   * Unwraps one layer of the type or just returns the type again if its not a wrapped type
   *
   * @param type the type to be unwrapped
   * @return the unwrapped type or the same type again if its not wrapped
   */
  public static Type unwrapOne(Type type) {
    if (isNonNull(type)) {
      return ((NonNullType)type).getType();
    }
    else if (isList(type)) {
      return ((ListType)type).getType();
    }
    return type;
  }

  /**
   * Returns {@code true} if the provided type is a non null type,
   * otherwise returns {@code false}.
   *
   * @param type the type to check
   * @return {@code true} if the provided type is a non null type
   * otherwise {@code false}
   */
  public static boolean isNonNull(Type type) {
    return type instanceof NonNullType;
  }

  /**
   * Returns {@code true} if the provided type is a list type,
   * otherwise returns {@code false}.
   *
   * @param type the type to check
   * @return {@code true} if the provided type is a list typ,
   * otherwise {@code false}
   */
  public static boolean isList(Type type) {
    return type instanceof ListType;
  }

  /**
   * Returns {@code true} if the given type is a non null or list type,
   * that is a wrapped type, otherwise returns {@code false}.
   *
   * @param type the type to check
   * @return {@code true} if the given type is a non null or list type,
   * otherwise {@code false}
   */
  public static boolean isWrapped(Type type) {
    return isList(type) || isNonNull(type);
  }
}
