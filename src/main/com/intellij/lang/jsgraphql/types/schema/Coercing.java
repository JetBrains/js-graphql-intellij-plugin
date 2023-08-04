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


import com.intellij.lang.jsgraphql.types.PublicSpi;

import java.util.Map;

/**
 * The Coercing interface is used by {@link com.intellij.lang.jsgraphql.types.schema.GraphQLScalarType}s to parse and serialise object values.
 * <p>
 * There are two major responsibilities, result coercion and input coercion.
 * <p>
 * Result coercion is taking a value from a Java object and coercing it into the constraints of the scalar type.
 * For example imagine a DateTime scalar, the result coercion would need to take an object and turn it into a
 * ISO date or throw an exception if it cant.
 * <p>
 * Input coercion is taking a value that came in from requests variables or hard coded query literals and coercing them into a
 * Java object value that is acceptable to the scalar type.  Again using the DateTime example, the input coercion would try to
 * parse an ISO date time object or throw an exception if it cant.
 * <p>
 * See http://facebook.github.io/graphql/#sec-Scalars
 */
@PublicSpi
public interface Coercing<I, O> {

  /**
   * Called to convert a Java object result of a DataFetcher to a valid runtime value for the scalar type.
   * <p>
   * Note : Throw {@link com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException} if there is fundamental
   * problem during serialisation, don't return null to indicate failure.
   * <p>
   * Note : You should not allow {@link RuntimeException}s to come out of your serialize method, but rather
   * catch them and fire them as {@link com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException} instead as per the method contract.
   *
   * @param dataFetcherResult is never null
   * @return a serialized value which may be null.
   * @throws com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException if value input can't be serialized
   */
  O serialize(Object dataFetcherResult) throws CoercingSerializeException;

  /**
   * Called to resolve an input from a query variable into a Java object acceptable for the scalar type.
   * <p>
   * Note : You should not allow {@link RuntimeException}s to come out of your parseValue method, but rather
   * catch them and fire them as {@link com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException} instead as per the method contract.
   *
   * @param input is never null
   * @return a parsed value which is never null
   * @throws com.intellij.lang.jsgraphql.types.schema.CoercingParseValueException if value input can't be parsed
   */
  I parseValue(Object input) throws CoercingParseValueException;

  /**
   * Called during query validation to convert a query input AST node into a Java object acceptable for the scalar type.  The input
   * object will be an instance of {@link com.intellij.lang.jsgraphql.types.language.Value}.
   * <p>
   * Note : You should not allow {@link RuntimeException}s to come out of your parseLiteral method, but rather
   * catch them and fire them as {@link com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException} instead as per the method contract.
   *
   * @param input is never null
   * @return a parsed value which is never null
   * @throws com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException if input literal can't be parsed
   */
  I parseLiteral(Object input) throws CoercingParseLiteralException;

  /**
   * Called during query execution to convert a query input AST node into a Java object acceptable for the scalar type.  The input
   * object will be an instance of {@link com.intellij.lang.jsgraphql.types.language.Value}.
   * <p>
   * Note : You should not allow {@link RuntimeException}s to come out of your parseLiteral method, but rather
   * catch them and fire them as {@link com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException} instead as per the method contract.
   * <p>
   * Many scalar types don't need to implement this method because they don't take AST {@link com.intellij.lang.jsgraphql.types.language.VariableReference}
   * objects and convert them into actual values.  But for those scalar types that want to do this, then this
   * method should be implemented.
   *
   * @param input     is never null
   * @param variables the resolved variables passed to the query
   * @return a parsed value which is never null
   * @throws com.intellij.lang.jsgraphql.types.schema.CoercingParseLiteralException if input literal can't be parsed
   */
  @SuppressWarnings("unused")
  default I parseLiteral(Object input, Map<String, Object> variables) throws CoercingParseLiteralException {
    return parseLiteral(input);
  }
}
