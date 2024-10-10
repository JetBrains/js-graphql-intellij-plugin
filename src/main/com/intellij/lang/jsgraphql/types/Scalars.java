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
package com.intellij.lang.jsgraphql.types;


import com.intellij.lang.jsgraphql.types.scalar.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType;

/**
 * This contains the implementations of the Scalar types that ship with graphql-java.  Some are proscribed
 * by the graphql specification (Int, Float, String, Boolean and ID) while others are offer because they are common on
 * Java platforms.
 * <p>
 * For more info see http://graphql.org/learn/schema/#scalar-types and more specifically http://facebook.github.io/graphql/#sec-Scalars
 */
@PublicApi
public class Scalars {

  /**
   * This represents the "Int" type as defined in the graphql specification : http://facebook.github.io/graphql/#sec-Int
   * <p>
   * The Int scalar type represents a signed 32‐bit numeric non‐fractional value.
   */
  public static final GraphQLScalarType GraphQLInt = GraphQLScalarType.newScalar()
    .name("Int").description("Built-in Int").coercing(new GraphqlIntCoercing()).build();

  /**
   * This represents the "Float" type as defined in the graphql specification : http://facebook.github.io/graphql/#sec-Float
   * <p>
   * Note: The Float type in GraphQL is equivalent to Double in Java. (double precision IEEE 754)
   */
  public static final GraphQLScalarType GraphQLFloat = GraphQLScalarType.newScalar()
    .name("Float").description("Built-in Float").coercing(new GraphqlFloatCoercing()).build();

  /**
   * This represents the "String" type as defined in the graphql specification : http://facebook.github.io/graphql/#sec-String
   */
  public static final GraphQLScalarType GraphQLString = GraphQLScalarType.newScalar()
    .name("String").description("Built-in String").coercing(new GraphqlStringCoercing()).build();

  /**
   * This represents the "Boolean" type as defined in the graphql specification : http://facebook.github.io/graphql/#sec-Boolean
   */
  public static final GraphQLScalarType GraphQLBoolean = GraphQLScalarType.newScalar()
    .name("Boolean").description("Built-in Boolean").coercing(new GraphqlBooleanCoercing()).build();

  /**
   * This represents the "ID" type as defined in the graphql specification : http://facebook.github.io/graphql/#sec-ID
   * <p>
   * The ID scalar type represents a unique identifier, often used to re-fetch an object or as the key for a cache. The
   * ID type is serialized in the same way as a String; however, it is not intended to be human‐readable. While it is
   * often numeric, it should always serialize as a String.
   */
  public static final GraphQLScalarType GraphQLID = GraphQLScalarType.newScalar()
    .name("ID").description("Built-in ID").coercing(new GraphqlIDCoercing()).build();

  /**
   * This represents the "Long" type which is a representation of java.lang.Long
   *
   * @deprecated The is a non standard scalar and is difficult for clients (such as browser and mobile code) to cope with
   * the exact semantics.  These will be removed in the future version and moved to another library.
   */
  @Deprecated(forRemoval = true)
  public static final GraphQLScalarType GraphQLLong = GraphQLScalarType.newScalar()
    .name("Long").description("Long type").coercing(new GraphqlLongCoercing()).build();

  /**
   * This represents the "Short" type which is a representation of java.lang.Short
   *
   * @deprecated The is a non standard scalar and is difficult for clients (such as browser and mobile code) to cope with
   * the exact semantics.  These will be removed in the future version and moved to another library.
   */
  @Deprecated
  public static final GraphQLScalarType GraphQLShort = GraphQLScalarType.newScalar()
    .name("Short").description("Built-in Short as Int").coercing(new GraphqlShortCoercing()).build();

  /**
   * This represents the "Byte" type which is a representation of java.lang.Byte
   *
   * @deprecated The is a non standard scalar and is difficult for clients (such as browser and mobile code) to cope with
   * the exact semantics.  These will be removed in the future version and moved to another library.
   */
  @Deprecated
  public static final GraphQLScalarType GraphQLByte = GraphQLScalarType.newScalar()
    .name("Byte").description("Built-in Byte as Int").coercing(new GraphqlByteCoercing()).build();


  /**
   * This represents the "BigInteger" type which is a representation of java.math.BigInteger
   *
   * @deprecated The is a non standard scalar and is difficult for clients (such as browser and mobile code) to cope with
   * the exact semantics.  These will be removed in the future version and moved to another library.
   */
  @Deprecated
  public static final GraphQLScalarType GraphQLBigInteger = GraphQLScalarType.newScalar()
    .name("BigInteger").description("Built-in java.math.BigInteger").coercing(new GraphqlBigIntegerCoercing()).build();

  /**
   * This represents the "BigDecimal" type which is a representation of java.math.BigDecimal
   *
   * @deprecated The is a non standard scalar and is difficult for clients (such as browser and mobile code) to cope with
   * the exact semantics.  These will be removed in the future version and moved to another library.
   */
  @Deprecated
  public static final GraphQLScalarType GraphQLBigDecimal = GraphQLScalarType.newScalar()
    .name("BigDecimal").description("Built-in java.math.BigDecimal").coercing(new GraphqlBigDecimalCoercing()).build();


  /**
   * This represents the "Char" type which is a representation of java.lang.Character
   *
   * @deprecated The is a non standard scalar and is difficult for clients (such as browser and mobile code) to cope with
   * the exact semantics.  These will be removed in the future version and moved to another library.
   */
  @Deprecated(forRemoval = true)
  public static final GraphQLScalarType GraphQLChar = GraphQLScalarType.newScalar()
    .name("Char").description("Built-in Char as Character").coercing(new GraphqlCharCoercing()).build();
}
