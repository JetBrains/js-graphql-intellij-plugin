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

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphqlErrorException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

@PublicApi
public class CoercingParseValueException extends GraphqlErrorException {

  public CoercingParseValueException() {
    this(newCoercingParseValueException());
  }

  public CoercingParseValueException(String message) {
    this(newCoercingParseValueException().message(message));
  }

  public CoercingParseValueException(String message, Throwable cause) {
    this(newCoercingParseValueException().message(message).cause(cause));
  }

  public CoercingParseValueException(Throwable cause) {
    this(newCoercingParseValueException().cause(cause));
  }

  public CoercingParseValueException(String message, Throwable cause, SourceLocation sourceLocation) {
    this(newCoercingParseValueException().message(message).cause(cause).sourceLocation(sourceLocation));
  }

  private CoercingParseValueException(Builder builder) {
    super(builder);
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.ValidationError;
  }

  public static Builder newCoercingParseValueException() {
    return new Builder();
  }

  public static class Builder extends BuilderBase<Builder, CoercingParseValueException> {
    public CoercingParseValueException build() {
      return new CoercingParseValueException(this);
    }
  }
}
