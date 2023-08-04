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

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.Collections;
import java.util.Map;

/**
 * Parameters sent to {@link Instrumentation} methods
 */
@PublicApi
public class InstrumentationExecutionParameters {
  private final ExecutionInput executionInput;
  private final String query;
  private final String operation;
  private final Object context;
  private final Map<String, Object> variables;
  private final InstrumentationState instrumentationState;
  private final GraphQLSchema schema;

  public InstrumentationExecutionParameters(ExecutionInput executionInput,
                                            GraphQLSchema schema,
                                            InstrumentationState instrumentationState) {
    this.executionInput = executionInput;
    this.query = executionInput.getQuery();
    this.operation = executionInput.getOperationName();
    this.context = executionInput.getContext();
    this.variables = executionInput.getVariables() != null ? executionInput.getVariables() : Collections.emptyMap();
    this.instrumentationState = instrumentationState;
    this.schema = schema;
  }

  /**
   * Returns a cloned parameters object with the new state
   *
   * @param instrumentationState the new state for this parameters object
   * @return a new parameters object with the new state
   */
  public InstrumentationExecutionParameters withNewState(InstrumentationState instrumentationState) {
    return new InstrumentationExecutionParameters(this.getExecutionInput(), this.schema, instrumentationState);
  }

  public ExecutionInput getExecutionInput() {
    return executionInput;
  }

  public String getQuery() {
    return query;
  }

  public String getOperation() {
    return operation;
  }

  @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
  public <T> T getContext() {
    return (T)context;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public <T extends InstrumentationState> T getInstrumentationState() {
    //noinspection unchecked
    return (T)instrumentationState;
  }

  public GraphQLSchema getSchema() {
    return this.schema;
  }
}
