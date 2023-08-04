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


import java.util.List;
import java.util.Map;

/**
 * This simple value class represents the result of performing a graphql query.
 */
@PublicApi
@SuppressWarnings("TypeParameterUnusedInFormals")
public interface ExecutionResult {

  /**
   * @return the errors that occurred during execution or empty list if there is none
   */
  List<GraphQLError> getErrors();

  /**
   * @param <T> allows type coercion
   * @return the data in the result or null if there is none
   */
  <T> T getData();

  /**
   * The graphql specification specifies:
   * <p>
   * "If an error was encountered before execution begins, the data entry should not be present in the result.
   * If an error was encountered during the execution that prevented a valid response, the data entry in the response should be null."
   * <p>
   * This allows to distinguish between the cases where {@link #getData()} returns null.
   * <p>
   * See : <a href="https://graphql.github.io/graphql-spec/June2018/#sec-Data">https://graphql.github.io/graphql-spec/June2018/#sec-Data</a>
   *
   * @return <code>true</code> if the entry "data" should be present in the result
   * <code>false</code> otherwise
   */
  boolean isDataPresent();

  /**
   * @return a map of extensions or null if there are none
   */
  Map<Object, Object> getExtensions();


  /**
   * The graphql specification says that result of a call should be a map that follows certain rules on what items
   * should be present.  Certain JSON serializers may or may interpret {@link ExecutionResult} to spec, so this method
   * is provided to produce a map that strictly follows the specification.
   * <p>
   * See : <a href="http://facebook.github.io/graphql/#sec-Response-Format">http://facebook.github.io/graphql/#sec-Response-Format</a>
   *
   * @return a map of the result that strictly follows the spec
   */
  Map<String, Object> toSpecification();
}
