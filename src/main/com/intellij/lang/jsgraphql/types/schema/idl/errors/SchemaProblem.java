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
package com.intellij.lang.jsgraphql.types.schema.idl.errors;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * A number of problems can occur when using the schema tools like {@link SchemaParser}
 * or {@link com.intellij.lang.jsgraphql.types.schema.idl.SchemaGenerator} classes and they are reported via this
 * exception as a list of {@link GraphQLError}s
 */
@Internal
public class SchemaProblem extends GraphQLException {

  private final List<GraphQLError> errors;

  public SchemaProblem(List<GraphQLError> errors) {
    this.errors = new ArrayList<>(errors);
  }

  @Override
  public String getMessage() {
    return "errors=" + errors;
  }

  public List<GraphQLError> getErrors() {
    return errors;
  }

  @Override
  public String toString() {
    return "SchemaProblem{" +
           "errors=" + errors +
           '}';
  }
}
