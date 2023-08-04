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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A base class for graphql runtime exceptions that also implement {@link com.intellij.lang.jsgraphql.types.GraphQLError} and can be used
 * in a general sense direct or have specialisations made of it.
 * <p>
 * This is aimed amongst other reasons at Kotlin consumers due to https://github.com/graphql-java/graphql-java/issues/1690
 * as well as being a way to share common code.
 */
@PublicApi
public class GraphqlErrorException extends GraphQLException implements GraphQLError {

  private final List<SourceLocation> locations;
  private final Map<String, Object> extensions;
  private final List<Object> path;
  private final ErrorClassification errorClassification;

  protected GraphqlErrorException(BuilderBase<?, ?> builder) {
    super(builder.message, builder.cause);
    this.locations = builder.sourceLocations;
    this.extensions = builder.extensions;
    this.path = builder.path;
    this.errorClassification = builder.errorClassification;
  }

  @Override
  public List<SourceLocation> getLocations() {
    return locations;
  }

  @Override
  public ErrorClassification getErrorType() {
    return errorClassification;
  }

  @Override
  public List<Object> getPath() {
    return path;
  }

  @Override
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  public static Builder newErrorException() {
    return new Builder();
  }

  public static class Builder extends BuilderBase<Builder, GraphqlErrorException> {
    public GraphqlErrorException build() {
      return new GraphqlErrorException(this);
    }
  }

  /**
   * A trait like base class that contains the properties that GraphqlErrorException handles and can
   * be used by other classes to derive their own builders.
   *
   * @param <T> the derived class
   * @param <B> the class to be built
   */
  protected abstract static class BuilderBase<T extends BuilderBase<T, B>, B extends GraphqlErrorException> {
    protected String message;
    protected Throwable cause;
    protected ErrorClassification errorClassification = ErrorType.DataFetchingException;
    protected List<SourceLocation> sourceLocations;
    protected Map<String, Object> extensions;
    protected List<Object> path;

    private T asDerivedType() {
      //noinspection unchecked
      return (T)this;
    }

    public T message(String message) {
      this.message = message;
      return asDerivedType();
    }

    public T cause(Throwable cause) {
      this.cause = cause;
      return asDerivedType();
    }

    public T sourceLocation(SourceLocation sourceLocation) {
      return sourceLocations(sourceLocation == null ? null : Collections.singletonList(sourceLocation));
    }

    public T sourceLocations(List<SourceLocation> sourceLocations) {
      this.sourceLocations = sourceLocations;
      return asDerivedType();
    }

    public T errorClassification(ErrorClassification errorClassification) {
      this.errorClassification = errorClassification;
      return asDerivedType();
    }

    public T path(List<Object> path) {
      this.path = path;
      return asDerivedType();
    }

    public T extensions(Map<String, Object> extensions) {
      this.extensions = extensions;
      return asDerivedType();
    }

    public abstract B build();
  }
}
