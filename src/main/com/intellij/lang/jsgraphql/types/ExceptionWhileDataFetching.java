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


import com.intellij.lang.jsgraphql.types.execution.ResultPath;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static java.lang.String.format;

/**
 * This graphql error will be used if a runtime exception is encountered while a data fetcher is invoked
 */
@PublicApi
public class ExceptionWhileDataFetching implements GraphQLError {

  private final String message;
  private final List<Object> path;
  private final Throwable exception;
  private final List<SourceLocation> locations;
  private final Map<String, Object> extensions;

  public ExceptionWhileDataFetching(ResultPath path, Throwable exception, SourceLocation sourceLocation) {
    this.path = assertNotNull(path).toList();
    this.exception = assertNotNull(exception);
    this.locations = Collections.singletonList(sourceLocation);
    this.extensions = mkExtensions(exception);
    this.message = mkMessage(path, exception);
  }

  private String mkMessage(ResultPath path, Throwable exception) {
    return format("Exception while fetching data (%s) : %s", path, exception.getMessage());
  }

  /*
   * This allows a DataFetcher to throw a graphql error and have "extension data" be transferred from that
   * exception into the ExceptionWhileDataFetching error and hence have custom "extension attributes"
   * per error message.
   */
  private Map<String, Object> mkExtensions(Throwable exception) {
    Map<String, Object> extensions = null;
    if (exception instanceof GraphQLError) {
      Map<String, Object> map = ((GraphQLError)exception).getExtensions();
      if (map != null) {
        extensions = new LinkedHashMap<>(map);
      }
    }
    return extensions;
  }

  public Throwable getException() {
    return exception;
  }


  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public List<SourceLocation> getLocations() {
    return locations;
  }

  @Override
  public List<Object> getPath() {
    return path;
  }

  @Override
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  @Override
  public ErrorType getErrorType() {
    return ErrorType.DataFetchingException;
  }

  @Override
  public String toString() {
    return "ExceptionWhileDataFetching{" +
           "path=" + path +
           ", exception=" + exception +
           ", locations=" + locations +
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
