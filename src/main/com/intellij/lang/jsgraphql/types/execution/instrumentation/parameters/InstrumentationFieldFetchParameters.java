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
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

/**
 * Parameters sent to {@link Instrumentation} methods
 */
@PublicApi
public class InstrumentationFieldFetchParameters extends InstrumentationFieldParameters {
  private final DataFetchingEnvironment environment;
  private final ExecutionStrategyParameters executionStrategyParameters;
  private final boolean trivialDataFetcher;

  public InstrumentationFieldFetchParameters(ExecutionContext getExecutionContext,
                                             GraphQLFieldDefinition fieldDef,
                                             DataFetchingEnvironment environment,
                                             ExecutionStrategyParameters executionStrategyParameters,
                                             boolean trivialDataFetcher) {
    super(getExecutionContext, environment::getExecutionStepInfo);
    this.environment = environment;
    this.executionStrategyParameters = executionStrategyParameters;
    this.trivialDataFetcher = trivialDataFetcher;
  }

  private InstrumentationFieldFetchParameters(ExecutionContext getExecutionContext,
                                              DataFetchingEnvironment environment,
                                              InstrumentationState instrumentationState,
                                              ExecutionStrategyParameters executionStrategyParameters,
                                              boolean trivialDataFetcher) {
    super(getExecutionContext, environment::getExecutionStepInfo, instrumentationState);
    this.environment = environment;
    this.executionStrategyParameters = executionStrategyParameters;
    this.trivialDataFetcher = trivialDataFetcher;
  }

  /**
   * Returns a cloned parameters object with the new state
   *
   * @param instrumentationState the new state for this parameters object
   * @return a new parameters object with the new state
   */
  @Override
  public InstrumentationFieldFetchParameters withNewState(InstrumentationState instrumentationState) {
    return new InstrumentationFieldFetchParameters(
      this.getExecutionContext(), this.getEnvironment(),
      instrumentationState, executionStrategyParameters, trivialDataFetcher);
  }


  public DataFetchingEnvironment getEnvironment() {
    return environment;
  }

  public boolean isTrivialDataFetcher() {
    return trivialDataFetcher;
  }
}
