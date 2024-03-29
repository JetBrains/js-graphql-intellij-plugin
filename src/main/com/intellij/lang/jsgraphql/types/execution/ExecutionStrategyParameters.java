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

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * The parameters that are passed to execution strategies
 */
@PublicApi
public class ExecutionStrategyParameters {
  private final ExecutionStepInfo executionStepInfo;
  private final Object source;
  private final Object localContext;
  private final MergedSelectionSet fields;
  private final NonNullableFieldValidator nonNullableFieldValidator;
  private final ResultPath path;
  private final MergedField currentField;
  private final int listSize;
  private final int currentListIndex;
  private final ExecutionStrategyParameters parent;

  private ExecutionStrategyParameters(ExecutionStepInfo executionStepInfo,
                                      Object source,
                                      Object localContext,
                                      MergedSelectionSet fields,
                                      NonNullableFieldValidator nonNullableFieldValidator,
                                      ResultPath path,
                                      MergedField currentField,
                                      int listSize,
                                      int currentListIndex,
                                      ExecutionStrategyParameters parent) {

    this.executionStepInfo = assertNotNull(executionStepInfo, () -> "executionStepInfo is null");
    this.localContext = localContext;
    this.fields = assertNotNull(fields, () -> "fields is null");
    this.source = source;
    this.nonNullableFieldValidator = nonNullableFieldValidator;
    this.path = path;
    this.currentField = currentField;
    this.listSize = listSize;
    this.currentListIndex = currentListIndex;
    this.parent = parent;
  }

  public ExecutionStepInfo getExecutionStepInfo() {
    return executionStepInfo;
  }

  public Object getSource() {
    return source;
  }

  public MergedSelectionSet getFields() {
    return fields;
  }

  public NonNullableFieldValidator getNonNullFieldValidator() {
    return nonNullableFieldValidator;
  }

  public ResultPath getPath() {
    return path;
  }

  public Object getLocalContext() {
    return localContext;
  }

  public int getListSize() {
    return listSize;
  }

  public int getCurrentListIndex() {
    return currentListIndex;
  }

  public ExecutionStrategyParameters getParent() {
    return parent;
  }

  /**
   * This returns the current field in its query representations.
   *
   * @return the current merged fields
   */
  public MergedField getField() {
    return currentField;
  }

  public ExecutionStrategyParameters transform(Consumer<Builder> builderConsumer) {
    Builder builder = newParameters(this);
    builderConsumer.accept(builder);
    return builder.build();
  }

  @Override
  public String toString() {
    return String.format("ExecutionStrategyParameters { path=%s, executionStepInfo=%s, source=%s, fields=%s }",
                         path, executionStepInfo, source, fields);
  }

  public static Builder newParameters() {
    return new Builder();
  }

  public static Builder newParameters(ExecutionStrategyParameters oldParameters) {
    return new Builder(oldParameters);
  }

  public static class Builder {
    ExecutionStepInfo executionStepInfo;
    Object source;
    Object localContext;
    MergedSelectionSet fields;
    NonNullableFieldValidator nonNullableFieldValidator;
    ResultPath path = ResultPath.rootPath();
    MergedField currentField;
    int listSize;
    int currentListIndex;
    ExecutionStrategyParameters parent;

    /**
     * @see ExecutionStrategyParameters#newParameters()
     */
    private Builder() {
    }

    /**
     * @see ExecutionStrategyParameters#newParameters(ExecutionStrategyParameters)
     */
    private Builder(ExecutionStrategyParameters oldParameters) {
      this.executionStepInfo = oldParameters.executionStepInfo;
      this.source = oldParameters.source;
      this.localContext = oldParameters.localContext;
      this.fields = oldParameters.fields;
      this.nonNullableFieldValidator = oldParameters.nonNullableFieldValidator;
      this.currentField = oldParameters.currentField;
      this.path = oldParameters.path;
      this.parent = oldParameters.parent;
      this.listSize = oldParameters.listSize;
      this.currentListIndex = oldParameters.currentListIndex;
    }

    public Builder executionStepInfo(ExecutionStepInfo executionStepInfo) {
      this.executionStepInfo = executionStepInfo;
      return this;
    }

    public Builder executionStepInfo(ExecutionStepInfo.Builder executionStepInfoBuilder) {
      this.executionStepInfo = executionStepInfoBuilder.build();
      return this;
    }

    public Builder fields(MergedSelectionSet fields) {
      this.fields = fields;
      return this;
    }

    public Builder field(MergedField currentField) {
      this.currentField = currentField;
      return this;
    }

    public Builder source(Object source) {
      this.source = source;
      return this;
    }

    public Builder localContext(Object localContext) {
      this.localContext = localContext;
      return this;
    }

    public Builder nonNullFieldValidator(NonNullableFieldValidator nonNullableFieldValidator) {
      this.nonNullableFieldValidator = Assert.assertNotNull(nonNullableFieldValidator, () -> "requires a NonNullValidator");
      return this;
    }

    public Builder path(ResultPath path) {
      this.path = path;
      return this;
    }

    public Builder listSize(int listSize) {
      this.listSize = listSize;
      return this;
    }

    public Builder currentListIndex(int currentListIndex) {
      this.currentListIndex = currentListIndex;
      return this;
    }

    public Builder parent(ExecutionStrategyParameters parent) {
      this.parent = parent;
      return this;
    }


    public ExecutionStrategyParameters build() {
      return new ExecutionStrategyParameters(executionStepInfo, source, localContext, fields, nonNullableFieldValidator, path, currentField,
                                             listSize, currentListIndex, parent);
    }
  }
}
