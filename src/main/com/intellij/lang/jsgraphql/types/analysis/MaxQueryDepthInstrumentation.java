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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.AbortExecutionException;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationValidationParameters;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;

import java.util.List;
import java.util.function.Function;

import static com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentationContext.whenCompleted;

/**
 * Prevents execution if the query depth is greater than the specified maxDepth.
 * <p>
 * Use the {@code Function<QueryDepthInfo, Boolean>} parameter to supply a function to perform a custom action when the max depth is
 * exceeded. If the function returns {@code true} a {@link AbortExecutionException} is thrown.
 */
@PublicApi
public class MaxQueryDepthInstrumentation extends SimpleInstrumentation {

  private final int maxDepth;
  private final Function<QueryDepthInfo, Boolean> maxQueryDepthExceededFunction;

  /**
   * Creates a new instrumentation that tracks the query depth.
   *
   * @param maxDepth max allowed depth, otherwise execution will be aborted
   */
  public MaxQueryDepthInstrumentation(int maxDepth) {
    this(maxDepth, (queryDepthInfo) -> true);
  }

  /**
   * Creates a new instrumentation that tracks the query depth.
   *
   * @param maxDepth                      max allowed depth, otherwise execution will be aborted
   * @param maxQueryDepthExceededFunction the function to perform when the max depth is exceeded
   */
  public MaxQueryDepthInstrumentation(int maxDepth, Function<QueryDepthInfo, Boolean> maxQueryDepthExceededFunction) {
    this.maxDepth = maxDepth;
    this.maxQueryDepthExceededFunction = maxQueryDepthExceededFunction;
  }

  @Override
  public InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
    return whenCompleted((errors, throwable) -> {
      if ((errors != null && errors.size() > 0) || throwable != null) {
        return;
      }
      QueryTraverser queryTraverser = newQueryTraverser(parameters);
      int depth = queryTraverser.reducePreOrder((env, acc) -> Math.max(getPathLength(env.getParentEnvironment()), acc), 0);
      if (depth > maxDepth) {
        QueryDepthInfo queryDepthInfo = QueryDepthInfo.newQueryDepthInfo()
          .depth(depth)
          .build();
        boolean throwAbortException = maxQueryDepthExceededFunction.apply(queryDepthInfo);
        if (throwAbortException) {
          throw mkAbortException(depth, maxDepth);
        }
      }
    });
  }

  /**
   * Called to generate your own error message or custom exception class
   *
   * @param depth    the depth of the query
   * @param maxDepth the maximum depth allowed
   * @return a instance of AbortExecutionException
   */
  protected AbortExecutionException mkAbortException(int depth, int maxDepth) {
    return new AbortExecutionException("maximum query depth exceeded " + depth + " > " + maxDepth);
  }

  QueryTraverser newQueryTraverser(InstrumentationValidationParameters parameters) {
    return QueryTraverser.newQueryTraverser()
      .schema(parameters.getSchema())
      .document(parameters.getDocument())
      .operationName(parameters.getOperation())
      .variables(parameters.getVariables())
      .build();
  }

  private int getPathLength(QueryVisitorFieldEnvironment path) {
    int length = 1;
    while (path != null) {
      path = path.getParentEnvironment();
      length++;
    }
    return length;
  }
}
