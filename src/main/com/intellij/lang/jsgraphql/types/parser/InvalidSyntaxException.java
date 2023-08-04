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
package com.intellij.lang.jsgraphql.types.parser;


import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.InvalidSyntaxError;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.Collections;
import java.util.List;

@Internal
public class InvalidSyntaxException extends GraphQLException {

  private final String message;
  private final String sourcePreview;
  private final String offendingToken;
  private final SourceLocation location;

  InvalidSyntaxException(SourceLocation location, String msg, String sourcePreview, String offendingToken, Exception cause) {
    super(cause);
    this.message = mkMessage(msg, offendingToken, location);
    this.sourcePreview = sourcePreview;
    this.offendingToken = offendingToken;
    this.location = location;
  }

  private String mkMessage(String msg, String offendingToken, SourceLocation location) {
    StringBuilder sb = new StringBuilder();
    sb.append("Invalid Syntax :");
    if (msg != null) {
      sb.append(" ").append(msg);
    }
    if (offendingToken != null) {
      sb.append(String.format(" offending token '%s'", offendingToken));
    }
    if (location != null) {
      sb.append(String.format(" at line %d column %d", location.getLine(), location.getColumn()));
    }
    return sb.toString();
  }

  public InvalidSyntaxError toInvalidSyntaxError() {
    List<SourceLocation> sourceLocations = location == null ? null : Collections.singletonList(location);
    return new InvalidSyntaxError(sourceLocations, message, sourcePreview, offendingToken);
  }


  @Override
  public String getMessage() {
    return message;
  }

  public SourceLocation getLocation() {
    return location;
  }

  public String getSourcePreview() {
    return sourcePreview;
  }

  public String getOffendingToken() {
    return offendingToken;
  }
}

