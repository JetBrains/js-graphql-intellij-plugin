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


import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;

@Internal
public class ExecutionResultImpl implements ExecutionResult {

  private final List<GraphQLError> errors;
  private final Object data;
  private final transient Map<Object, Object> extensions;
  private final transient boolean dataPresent;

  public ExecutionResultImpl(GraphQLError error) {
    this(false, null, Collections.singletonList(error), null);
  }

  public ExecutionResultImpl(List<? extends GraphQLError> errors) {
    this(false, null, errors, null);
  }

  public ExecutionResultImpl(Object data, List<? extends GraphQLError> errors) {
    this(true, data, errors, null);
  }

  public ExecutionResultImpl(Object data, List<? extends GraphQLError> errors, Map<Object, Object> extensions) {
    this(true, data, errors, extensions);
  }

  public ExecutionResultImpl(ExecutionResultImpl other) {
    this(other.dataPresent, other.data, other.errors, other.extensions);
  }

  private ExecutionResultImpl(boolean dataPresent, Object data, List<? extends GraphQLError> errors, Map<Object, Object> extensions) {
    this.dataPresent = dataPresent;
    this.data = data;

    if (errors != null && !errors.isEmpty()) {
      this.errors = ImmutableList.copyOf(errors);
    }
    else {
      this.errors = ImmutableKit.emptyList();
    }

    this.extensions = extensions;
  }

  public boolean isDataPresent() {
    return dataPresent;
  }

  @Override
  public List<GraphQLError> getErrors() {
    return errors;
  }

  @Override
  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T> T getData() {
    //noinspection unchecked
    return (T)data;
  }

  @Override
  public Map<Object, Object> getExtensions() {
    return extensions;
  }

  @Override
  public Map<String, Object> toSpecification() {
    Map<String, Object> result = new LinkedHashMap<>();
    if (errors != null && !errors.isEmpty()) {
      result.put("errors", errorsToSpec(errors));
    }
    if (dataPresent) {
      result.put("data", data);
    }
    if (extensions != null) {
      result.put("extensions", extensions);
    }
    return result;
  }

  private Object errorsToSpec(List<GraphQLError> errors) {
    return map(errors, GraphQLError::toSpecification);
  }

  @Override
  public String toString() {
    return "ExecutionResultImpl{" +
           "errors=" + errors +
           ", data=" + data +
           ", dataPresent=" + dataPresent +
           ", extensions=" + extensions +
           '}';
  }

  public ExecutionResultImpl transform(Consumer<Builder> builderConsumer) {
    Builder builder = newExecutionResult().from(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  public static Builder newExecutionResult() {
    return new Builder();
  }

  public static class Builder {
    private boolean dataPresent;
    private Object data;
    private List<GraphQLError> errors = new ArrayList<>();
    private Map<Object, Object> extensions;

    public Builder from(ExecutionResult executionResult) {
      dataPresent = executionResult.isDataPresent();
      data = executionResult.getData();
      errors = new ArrayList<>(executionResult.getErrors());
      extensions = executionResult.getExtensions();
      return this;
    }

    public Builder data(Object data) {
      dataPresent = true;
      this.data = data;
      return this;
    }

    public Builder errors(List<GraphQLError> errors) {
      this.errors = errors;
      return this;
    }

    public Builder addErrors(List<GraphQLError> errors) {
      this.errors.addAll(errors);
      return this;
    }

    public Builder addError(GraphQLError error) {
      this.errors.add(error);
      return this;
    }

    public Builder extensions(Map<Object, Object> extensions) {
      this.extensions = extensions;
      return this;
    }

    public Builder addExtension(String key, Object value) {
      this.extensions = (this.extensions == null ? new LinkedHashMap<>() : this.extensions);
      this.extensions.put(key, value);
      return this;
    }

    public ExecutionResultImpl build() {
      return new ExecutionResultImpl(dataPresent, data, errors, extensions);
    }
  }
}
