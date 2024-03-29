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

import com.intellij.lang.jsgraphql.types.execution.DataFetcherResult;
import com.intellij.lang.jsgraphql.types.execution.ResultPath;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * This helps you build {@link graphql.GraphQLError}s and also has a quick way to make a  {@link graphql.execution.DataFetcherResult}s
 * from that error.
 */
@PublicApi
public class GraphqlErrorBuilder {

  private String message;
  private List<Object> path;
  private List<SourceLocation> locations = new ArrayList<>();
  private ErrorClassification errorType = ErrorType.DataFetchingException;
  private Map<String, Object> extensions = null;


  /**
   * @return a builder of {@link graphql.GraphQLError}s
   */
  public static GraphqlErrorBuilder newError() {
    return new GraphqlErrorBuilder();
  }

  /**
   * This will set up the {@link GraphQLError#getLocations()} and {@link GraphQLError#getPath()} for you from the
   * fetching environment.
   *
   * @param dataFetchingEnvironment the data fetching environment
   * @return a builder of {@link GraphQLError}s
   */
  public static GraphqlErrorBuilder newError(DataFetchingEnvironment dataFetchingEnvironment) {
    return new GraphqlErrorBuilder()
      .location(dataFetchingEnvironment.getField().getSourceLocation())
      .path(dataFetchingEnvironment.getExecutionStepInfo().getPath());
  }

  private GraphqlErrorBuilder() {
  }

  public GraphqlErrorBuilder message(String message, Object... formatArgs) {
    if (formatArgs == null || formatArgs.length == 0) {
      this.message = assertNotNull(message);
    }
    else {
      this.message = String.format(assertNotNull(message), formatArgs);
    }
    return this;
  }

  public GraphqlErrorBuilder locations(List<SourceLocation> locations) {
    this.locations.addAll(assertNotNull(locations));
    return this;
  }

  public GraphqlErrorBuilder location(SourceLocation location) {
    this.locations.add(assertNotNull(location));
    return this;
  }

  public GraphqlErrorBuilder path(ResultPath path) {
    this.path = assertNotNull(path).toList();
    return this;
  }

  public GraphqlErrorBuilder path(List<Object> path) {
    this.path = assertNotNull(path);
    return this;
  }

  public GraphqlErrorBuilder errorType(ErrorClassification errorType) {
    this.errorType = assertNotNull(errorType);
    return this;
  }

  public GraphqlErrorBuilder extensions(Map<String, Object> extensions) {
    this.extensions = assertNotNull(extensions);
    return this;
  }

  /**
   * @return a newly built GraphqlError
   */
  public GraphQLError build() {
    assertNotNull(message, () -> "You must provide error message");
    return new GraphqlErrorImpl(message, locations, errorType, path, extensions);
  }

  private static class GraphqlErrorImpl implements GraphQLError {
    private final String message;
    private final List<SourceLocation> locations;
    private final ErrorClassification errorType;
    private final List<Object> path;
    private final Map<String, Object> extensions;

    public GraphqlErrorImpl(String message,
                            List<SourceLocation> locations,
                            ErrorClassification errorType,
                            List<Object> path,
                            Map<String, Object> extensions) {
      this.message = message;
      this.locations = locations;
      this.errorType = errorType;
      this.path = path;
      this.extensions = extensions;
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
    public ErrorClassification getErrorType() {
      return errorType;
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
    public String toString() {
      return message;
    }
  }

  /**
   * A helper method that allows you to return this error as a {@link graphql.execution.DataFetcherResult}
   *
   * @return a new data fetcher result that contains the built error
   */
  public DataFetcherResult toResult() {
    return DataFetcherResult.newResult()
      .error(build())
      .build();
  }
}
