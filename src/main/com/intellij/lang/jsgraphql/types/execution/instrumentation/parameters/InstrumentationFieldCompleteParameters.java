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
package com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStrategyParameters;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

import java.util.function.Supplier;

/**
 * Parameters sent to {@link graphql.execution.instrumentation.Instrumentation} methods
 */
@PublicApi
public class InstrumentationFieldCompleteParameters {
  private final ExecutionContext executionContext;
  private final Supplier<ExecutionStepInfo> executionStepInfo;
  private final Object fetchedValue;
  private final InstrumentationState instrumentationState;
  private final ExecutionStrategyParameters executionStrategyParameters;

  public InstrumentationFieldCompleteParameters(ExecutionContext executionContext,
                                                ExecutionStrategyParameters executionStrategyParameters,
                                                Supplier<ExecutionStepInfo> executionStepInfo,
                                                Object fetchedValue) {
    this(executionContext, executionStrategyParameters, executionStepInfo, fetchedValue, executionContext.getInstrumentationState());
  }

  InstrumentationFieldCompleteParameters(ExecutionContext executionContext,
                                         ExecutionStrategyParameters executionStrategyParameters,
                                         Supplier<ExecutionStepInfo> executionStepInfo,
                                         Object fetchedValue,
                                         InstrumentationState instrumentationState) {
    this.executionContext = executionContext;
    this.executionStrategyParameters = executionStrategyParameters;
    this.executionStepInfo = executionStepInfo;
    this.fetchedValue = fetchedValue;
    this.instrumentationState = instrumentationState;
  }

  /**
   * Returns a cloned parameters object with the new state
   *
   * @param instrumentationState the new state for this parameters object
   * @return a new parameters object with the new state
   */
  public InstrumentationFieldCompleteParameters withNewState(InstrumentationState instrumentationState) {
    return new InstrumentationFieldCompleteParameters(
      this.executionContext, executionStrategyParameters, this.executionStepInfo, this.fetchedValue, instrumentationState);
  }


  public ExecutionContext getExecutionContext() {
    return executionContext;
  }

  public ExecutionStrategyParameters getExecutionStrategyParameters() {
    return executionStrategyParameters;
  }

  public GraphQLFieldDefinition getField() {
    return getExecutionStepInfo().getFieldDefinition();
  }

  @Deprecated(forRemoval = true)
  public ExecutionStepInfo getTypeInfo() {
    return getExecutionStepInfo();
  }

  public ExecutionStepInfo getExecutionStepInfo() {
    return executionStepInfo.get();
  }

  public Object getFetchedValue() {
    return fetchedValue;
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T extends InstrumentationState> T getInstrumentationState() {
    //noinspection unchecked
    return (T)instrumentationState;
  }
}
