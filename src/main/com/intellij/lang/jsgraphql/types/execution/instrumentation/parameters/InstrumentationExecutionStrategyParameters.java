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
import com.intellij.lang.jsgraphql.types.execution.ExecutionStrategyParameters;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;

/**
 * Parameters sent to {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation} methods
 */
@PublicApi
public class InstrumentationExecutionStrategyParameters {

  private final ExecutionContext executionContext;
  private final ExecutionStrategyParameters executionStrategyParameters;
  private final InstrumentationState instrumentationState;

  public InstrumentationExecutionStrategyParameters(ExecutionContext executionContext,
                                                    ExecutionStrategyParameters executionStrategyParameters) {
    this(executionContext, executionStrategyParameters, executionContext.getInstrumentationState());
  }

  private InstrumentationExecutionStrategyParameters(ExecutionContext executionContext,
                                                     ExecutionStrategyParameters executionStrategyParameters,
                                                     InstrumentationState instrumentationState) {
    this.executionContext = executionContext;
    this.executionStrategyParameters = executionStrategyParameters;
    this.instrumentationState = instrumentationState;
  }

  /**
   * Returns a cloned parameters object with the new state
   *
   * @param instrumentationState the new state for this parameters object
   * @return a new parameters object with the new state
   */
  public InstrumentationExecutionStrategyParameters withNewState(InstrumentationState instrumentationState) {
    return new InstrumentationExecutionStrategyParameters(executionContext, executionStrategyParameters, instrumentationState);
  }

  public ExecutionContext getExecutionContext() {
    return executionContext;
  }

  public ExecutionStrategyParameters getExecutionStrategyParameters() {
    return executionStrategyParameters;
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T extends InstrumentationState> T getInstrumentationState() {
    //noinspection unchecked
    return (T)instrumentationState;
  }
}
