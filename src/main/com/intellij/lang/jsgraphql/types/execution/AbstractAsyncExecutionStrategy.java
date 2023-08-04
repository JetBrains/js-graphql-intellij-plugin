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

import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.ExecutionResultImpl;
import com.intellij.lang.jsgraphql.types.PublicSpi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;


@PublicSpi
public abstract class AbstractAsyncExecutionStrategy extends ExecutionStrategy {

  public AbstractAsyncExecutionStrategy() {
  }

  public AbstractAsyncExecutionStrategy(DataFetcherExceptionHandler dataFetcherExceptionHandler) {
    super(dataFetcherExceptionHandler);
  }

  // This method is kept for backward compatibility. Prefer calling/overriding another handleResults method
  protected BiConsumer<List<ExecutionResult>, Throwable> handleResults(ExecutionContext executionContext,
                                                                       List<String> fieldNames,
                                                                       CompletableFuture<ExecutionResult> overallResult) {
    return (List<ExecutionResult> results, Throwable exception) -> {
      if (exception != null) {
        handleNonNullException(executionContext, overallResult, exception);
        return;
      }
      Map<String, Object> resolvedValuesByField = new LinkedHashMap<>(fieldNames.size());
      int ix = 0;
      for (ExecutionResult executionResult : results) {

        String fieldName = fieldNames.get(ix++);
        resolvedValuesByField.put(fieldName, executionResult.getData());
      }
      overallResult.complete(new ExecutionResultImpl(resolvedValuesByField, executionContext.getErrors()));
    };
  }
}
