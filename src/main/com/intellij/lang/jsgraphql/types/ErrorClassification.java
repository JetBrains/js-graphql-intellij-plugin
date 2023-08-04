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

/**
 * Errors in graphql-java can have a classification to help with the processing
 * of errors.  Custom {@link GraphQLError} implementations could use
 * custom classifications.
 * <p>
 * graphql-java ships with a standard set of error classifications via {@link ErrorType}
 */
@PublicApi
public interface ErrorClassification {

  /**
   * This is called to create a representation of the error classification
   * that can be put into the `extensions` map of the graphql error under the key 'classification'
   * when {@link GraphQLError#toSpecification()} is called
   *
   * @param error the error associated with this classification
   * @return an object representation of this error classification
   */
  @SuppressWarnings("unused")
  default Object toSpecification(GraphQLError error) {
    return String.valueOf(this);
  }
}
