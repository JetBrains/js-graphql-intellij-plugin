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

import com.intellij.lang.jsgraphql.types.ErrorClassification;
import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphqlErrorException;
import com.intellij.lang.jsgraphql.types.PublicApi;

@PublicApi
public class CoercingSerializeException extends GraphqlErrorException {

  public CoercingSerializeException() {
    this(newCoercingSerializeException());
  }

  public CoercingSerializeException(String message) {
    this(newCoercingSerializeException().message(message));
  }

  public CoercingSerializeException(String message, Throwable cause) {
    this(newCoercingSerializeException().message(message).cause(cause));
  }

  public CoercingSerializeException(Throwable cause) {
    this(newCoercingSerializeException().cause(cause));
  }

  private CoercingSerializeException(Builder builder) {
    super(builder);
  }

  @Override
  public ErrorClassification getErrorType() {
    return ErrorType.DataFetchingException;
  }

  public static Builder newCoercingSerializeException() {
    return new Builder();
  }

  public static class Builder extends BuilderBase<Builder, CoercingSerializeException> {
    public CoercingSerializeException build() {
      return new CoercingSerializeException(this);
    }
  }
}
