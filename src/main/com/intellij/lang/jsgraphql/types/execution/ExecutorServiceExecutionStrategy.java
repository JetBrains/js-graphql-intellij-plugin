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
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 *
 * <p>Deprecation Notice : This execution strategy does not support all of the graphql-java capabilities
 * such as data loader.  Since its so easy to create a data fetcher that uses
 * {@link CompletableFuture#supplyAsync(java.util.function.Supplier, java.util.concurrent.Executor)}
 * to make field fetching happen off thread we recommend that you use that instead of this class.  This class
 * will be removed in a future version.
 * </p>
 *
 * <p>ExecutorServiceExecutionStrategy uses an {@link ExecutorService} to parallelize the resolve.</p>
 *
 * Due to the nature of {@link #execute(ExecutionContext, ExecutionStrategyParameters)}  implementation, {@link ExecutorService}
 * MUST have the following 2 characteristics:
 * <ul>
 * <li>1. The underlying {@link java.util.concurrent.ThreadPoolExecutor} MUST have a reasonable {@code maximumPoolSize}
 * <li>2. The underlying {@link java.util.concurrent.ThreadPoolExecutor} SHALL NOT use its task queue.
 * </ul>
 *
 * <p>Failure to follow 1. and 2. can result in a very large number of threads created or hanging. (deadlock)</p>
 *
 * See {@code graphql.execution.ExecutorServiceExecutionStrategyTest} for example usage.
 *
 * @deprecated Use {@link graphql.execution.AsyncExecutionStrategy} and {@link CompletableFuture#supplyAsync(java.util.function.Supplier, java.util.concurrent.Executor)}
 * in your data fetchers instead.
 */
@PublicApi
@Deprecated
public class ExecutorServiceExecutionStrategy extends ExecutionStrategy {

    final ExecutorService executorService;

    public ExecutorServiceExecutionStrategy(ExecutorService executorService) {
        this(executorService, new SimpleDataFetcherExceptionHandler());
    }

    public ExecutorServiceExecutionStrategy(ExecutorService executorService, DataFetcherExceptionHandler dataFetcherExceptionHandler) {
        super(dataFetcherExceptionHandler);
        this.executorService = executorService;
    }


    @Override
    public CompletableFuture<ExecutionResult> execute(final ExecutionContext executionContext, final ExecutionStrategyParameters parameters) {
        if (executorService == null) {
            return new AsyncExecutionStrategy().execute(executionContext, parameters);
        }

        Instrumentation instrumentation = executionContext.getInstrumentation();
        InstrumentationExecutionStrategyParameters instrumentationParameters = new InstrumentationExecutionStrategyParameters(executionContext, parameters);
        InstrumentationContext<ExecutionResult> executionStrategyCtx = instrumentation.beginExecutionStrategy(instrumentationParameters);

        MergedSelectionSet fields = parameters.getFields();
        Map<String, Future<CompletableFuture<ExecutionResult>>> futures = new LinkedHashMap<>();
        for (String fieldName : fields.keySet()) {
            final MergedField currentField = fields.getSubField(fieldName);

            ResultPath fieldPath = parameters.getPath().segment(mkNameForPath(currentField));
            ExecutionStrategyParameters newParameters = parameters
                    .transform(builder -> builder.field(currentField).path(fieldPath));

            Callable<CompletableFuture<ExecutionResult>> resolveField = () -> resolveField(executionContext, newParameters);
            futures.put(fieldName, executorService.submit(resolveField));
        }

        CompletableFuture<ExecutionResult> overallResult = new CompletableFuture<>();
        executionStrategyCtx.onDispatched(overallResult);

        try {
            Map<String, Object> results = new LinkedHashMap<>();
            for (String fieldName : futures.keySet()) {
                ExecutionResult executionResult;
                try {
                    executionResult = futures.get(fieldName).get().join();
                } catch (CompletionException e) {
                    if (e.getCause() instanceof NonNullableFieldWasNullException) {
                        assertNonNullFieldPrecondition((NonNullableFieldWasNullException) e.getCause());
                        results = null;
                        break;
                    } else {
                        throw e;
                    }
                }
                results.put(fieldName, executionResult != null ? executionResult.getData() : null);
            }

            ExecutionResultImpl executionResult = new ExecutionResultImpl(results, executionContext.getErrors());
            overallResult.complete(executionResult);

            overallResult = overallResult.whenComplete(executionStrategyCtx::onCompleted);
            return overallResult;
        } catch (InterruptedException | ExecutionException e) {
            executionStrategyCtx.onCompleted(null, e);
            throw new GraphQLException(e);
        }
    }
}
