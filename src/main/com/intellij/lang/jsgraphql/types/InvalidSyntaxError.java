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


import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@Internal
public class InvalidSyntaxError implements GraphQLError {

  private final String message;
  private final String sourcePreview;
  private final String offendingToken;
  private final List<SourceLocation> locations = new ArrayList<>();

  public InvalidSyntaxError(SourceLocation sourceLocation, String msg) {
    this(singletonList(sourceLocation), msg);
  }

  public InvalidSyntaxError(List<SourceLocation> sourceLocations, String msg) {
    this(sourceLocations, msg, null, null);
  }

  public InvalidSyntaxError(List<SourceLocation> sourceLocations, String msg, String sourcePreview, String offendingToken) {
    this.message = msg;
    this.sourcePreview = sourcePreview;
    this.offendingToken = offendingToken;
    if (sourceLocations != null) {
      this.locations.addAll(sourceLocations);
    }
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public List<SourceLocation> getLocations() {
    return locations;
  }

  public String getSourcePreview() {
    return sourcePreview;
  }

  public String getOffendingToken() {
    return offendingToken;
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.InvalidSyntax;
  }

  @Override
  public String toString() {
    return "InvalidSyntaxError{" +
           " message=" + message +
           " ,offendingToken=" + offendingToken +
           " ,locations=" + locations +
           " ,sourcePreview=" + sourcePreview +
           '}';
  }


  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object o) {
    return GraphqlErrorHelper.equals(this, o);
  }

  @Override
  public int hashCode() {
    return GraphqlErrorHelper.hashCode(this);
  }
}
