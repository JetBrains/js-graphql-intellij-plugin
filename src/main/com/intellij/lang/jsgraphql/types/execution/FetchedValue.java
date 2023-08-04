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
package com.intellij.lang.jsgraphql.types.execution;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;

import java.util.List;
import java.util.function.Consumer;

@Internal
public class FetchedValue {
  private final Object fetchedValue;
  private final Object rawFetchedValue;
  private final Object localContext;
  private final ImmutableList<GraphQLError> errors;

  private FetchedValue(Object fetchedValue, Object rawFetchedValue, ImmutableList<GraphQLError> errors, Object localContext) {
    this.fetchedValue = fetchedValue;
    this.rawFetchedValue = rawFetchedValue;
    this.errors = errors;
    this.localContext = localContext;
  }

  /*
   * the unboxed value meaning not Optional, not DataFetcherResult etc
   */
  public Object getFetchedValue() {
    return fetchedValue;
  }

  public Object getRawFetchedValue() {
    return rawFetchedValue;
  }

  public List<GraphQLError> getErrors() {
    return errors;
  }

  public Object getLocalContext() {
    return localContext;
  }

  public FetchedValue transform(Consumer<Builder> builderConsumer) {
    Builder builder = newFetchedValue(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  @Override
  public String toString() {
    return "FetchedValue{" +
           "fetchedValue=" + fetchedValue +
           ", rawFetchedValue=" + rawFetchedValue +
           ", localContext=" + localContext +
           ", errors=" + errors +
           '}';
  }

  public static Builder newFetchedValue() {
    return new Builder();
  }

  public static Builder newFetchedValue(FetchedValue otherValue) {
    return new Builder()
      .fetchedValue(otherValue.getFetchedValue())
      .rawFetchedValue(otherValue.getRawFetchedValue())
      .errors(otherValue.getErrors())
      .localContext(otherValue.getLocalContext())
      ;
  }

  public static class Builder {

    private Object fetchedValue;
    private Object rawFetchedValue;
    private Object localContext;
    private ImmutableList<GraphQLError> errors = ImmutableList.of();

    public Builder fetchedValue(Object fetchedValue) {
      this.fetchedValue = fetchedValue;
      return this;
    }

    public Builder rawFetchedValue(Object rawFetchedValue) {
      this.rawFetchedValue = rawFetchedValue;
      return this;
    }

    public Builder localContext(Object localContext) {
      this.localContext = localContext;
      return this;
    }

    public Builder errors(List<GraphQLError> errors) {
      this.errors = ImmutableList.copyOf(errors);
      return this;
    }

    public FetchedValue build() {
      return new FetchedValue(fetchedValue, rawFetchedValue, errors, localContext);
    }
  }
}
