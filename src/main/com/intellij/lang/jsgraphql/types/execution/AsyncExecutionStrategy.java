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
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.ExecutionStrategyInstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;

/**
 * The standard graphql execution strategy that runs fields asynchronously non-blocking.
 */
@PublicApi
public class AsyncExecutionStrategy extends AbstractAsyncExecutionStrategy {

  /**
   * The standard graphql execution strategy that runs fields asynchronously
   */
  public AsyncExecutionStrategy() {
    super(new SimpleDataFetcherExceptionHandler());
  }

  /**
   * Creates a execution strategy that uses the provided exception handler
   *
   * @param exceptionHandler the exception handler to use
   */
  public AsyncExecutionStrategy(DataFetcherExceptionHandler exceptionHandler) {
    super(exceptionHandler);
  }

  @Override
  @SuppressWarnings("FutureReturnValueIgnored")
  public CompletableFuture<ExecutionResult> execute(ExecutionContext executionContext, ExecutionStrategyParameters parameters)
    throws NonNullableFieldWasNullException {

    Instrumentation instrumentation = executionContext.getInstrumentation();
    InstrumentationExecutionStrategyParameters instrumentationParameters =
      new InstrumentationExecutionStrategyParameters(executionContext, parameters);

    ExecutionStrategyInstrumentationContext executionStrategyCtx = instrumentation.beginExecutionStrategy(instrumentationParameters);

    MergedSelectionSet fields = parameters.getFields();
    Set<String> fieldNames = fields.keySet();
    List<CompletableFuture<FieldValueInfo>> futures = new ArrayList<>(fieldNames.size());
    List<String> resolvedFields = new ArrayList<>(fieldNames.size());
    for (String fieldName : fieldNames) {
      MergedField currentField = fields.getSubField(fieldName);

      ResultPath fieldPath = parameters.getPath().segment(mkNameForPath(currentField));
      ExecutionStrategyParameters newParameters = parameters
        .transform(builder -> builder.field(currentField).path(fieldPath).parent(parameters));

      resolvedFields.add(fieldName);
      CompletableFuture<FieldValueInfo> future = resolveFieldWithInfo(executionContext, newParameters);
      futures.add(future);
    }
    CompletableFuture<ExecutionResult> overallResult = new CompletableFuture<>();
    executionStrategyCtx.onDispatched(overallResult);

    Async.each(futures).whenComplete((completeValueInfos, throwable) -> {
      BiConsumer<List<ExecutionResult>, Throwable> handleResultsConsumer = handleResults(executionContext, resolvedFields, overallResult);
      if (throwable != null) {
        handleResultsConsumer.accept(null, throwable.getCause());
        return;
      }
      List<CompletableFuture<ExecutionResult>> executionResultFuture = map(completeValueInfos, FieldValueInfo::getFieldValue);
      executionStrategyCtx.onFieldValuesInfo(completeValueInfos);
      Async.each(executionResultFuture).whenComplete(handleResultsConsumer);
    }).exceptionally((ex) -> {
      // if there are any issues with combining/handling the field results,
      // complete the future at all costs and bubble up any thrown exception so
      // the execution does not hang.
      overallResult.completeExceptionally(ex);
      return null;
    });

    overallResult.whenComplete(executionStrategyCtx::onCompleted);
    return overallResult;
  }
}
