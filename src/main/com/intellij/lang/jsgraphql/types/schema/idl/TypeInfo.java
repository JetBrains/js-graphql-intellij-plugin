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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.util.containers.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLList.list;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLNonNull.nonNull;

/**
 * This helper gives you access to the type info given a type definition
 */
@SuppressWarnings("rawtypes")
@Internal
public class TypeInfo {

  public static TypeInfo typeInfo(@NotNull Type type) {
    return new TypeInfo(type);
  }

  private final @NotNull Type rawType;
  private final @NotNull TypeName typeName;
  private final @NotNull Stack<Class<?>> decoration = new Stack<>();

  private TypeInfo(@NotNull Type type) {
    this.rawType = assertNotNull(type, () -> "type must not be null");
    while (!(type instanceof TypeName)) {
      if (type instanceof NonNullType) {
        decoration.push(NonNullType.class);
        type = ((NonNullType)type).getType();
      }
      if (type instanceof ListType) {
        decoration.push(ListType.class);
        type = ((ListType)type).getType();
      }
    }
    this.typeName = (TypeName)type;
  }

  public @NotNull Type getRawType() {
    return rawType;
  }

  public @NotNull TypeName getTypeName() {
    return typeName;
  }

  public @NotNull String getName() {
    return typeName.getName();
  }

  public boolean isList() {
    return rawType instanceof ListType;
  }

  public boolean isNonNull() {
    return rawType instanceof NonNullType;
  }

  public boolean isPlain() {
    return !isList() && !isNonNull();
  }

  /**
   * This will rename the type with the specified new name but will preserve the wrapping that was present
   *
   * @param newName the new name of the type
   * @return a new type info rebuilt with the new name
   */
  public TypeInfo renameAs(String newName) {

    Type out = TypeName.newTypeName(newName).build();

    Stack<Class<?>> wrappingStack = new Stack<>();
    wrappingStack.addAll(this.decoration);
    while (!wrappingStack.isEmpty()) {
      Class<?> clazz = wrappingStack.pop();
      if (clazz.equals(NonNullType.class)) {
        out = NonNullType.newNonNullType(out).build();
      }
      if (clazz.equals(ListType.class)) {
        out = ListType.newListType(out).build();
      }
    }
    return typeInfo(out);
  }

  /**
   * This will decorate a graphql type with the original hierarchy of non null and list'ness
   * it originally contained in its definition type
   *
   * @param objectType this should be a graphql type that was originally built from this raw type
   * @param <T>        the type
   * @return the decorated type
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T extends GraphQLType> @Nullable T decorate(GraphQLType objectType) {
    if (objectType == null) return null;

    GraphQLType out = objectType;
    Stack<Class<?>> wrappingStack = new Stack<>();
    wrappingStack.addAll(this.decoration);
    while (!wrappingStack.isEmpty()) {
      Class<?> clazz = wrappingStack.pop();
      if (clazz.equals(NonNullType.class)) {
        out = nonNull(out);
      }
      if (clazz.equals(ListType.class)) {
        out = list(out);
      }
    }
    // we handle both input and output graphql types
    //noinspection unchecked
    return (T)out;
  }

  public static String getAstDesc(Type type) {
    return AstPrinter.printAst(type);
  }

  public TypeInfo unwrapOne() {
    if (rawType instanceof NonNullType) {
      return typeInfo(((NonNullType)rawType).getType());
    }
    if (rawType instanceof ListType) {
      return typeInfo(((ListType)rawType).getType());
    }
    return this;
  }

  public Type unwrapOneType() {
    return unwrapOne().getRawType();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TypeInfo typeInfo = (TypeInfo)o;
    return isNonNull() == typeInfo.isNonNull() &&
           isList() == typeInfo.isList() &&
           Objects.equals(typeName.getName(), typeInfo.typeName.getName());
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(typeName.getName());
    result = 31 * result + Boolean.hashCode(isNonNull());
    result = 31 * result + Boolean.hashCode(isList());
    return result;
  }

  @Override
  public String toString() {
    return "TypeInfo{" +
           getAstDesc(rawType) +
           '}';
  }
}

