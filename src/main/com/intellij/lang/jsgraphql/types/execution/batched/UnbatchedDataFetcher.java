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
package com.intellij.lang.jsgraphql.types.execution.batched;


import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.Async;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;
import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;

/**
 * Given a normal data fetcher as a delegate,
 * uses that fetcher in a batched context by iterating through each source value and calling
 * the delegate.
 *
 * @deprecated This has been deprecated in favour of using {@link graphql.execution.AsyncExecutionStrategy} and {@link graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation}
 */
@Deprecated
@PublicApi
public class UnbatchedDataFetcher implements BatchedDataFetcher {

    private final DataFetcher delegate;

    public UnbatchedDataFetcher(DataFetcher delegate) {
        this.delegate = delegate;
    }


    @Override
    public CompletableFuture<List<Object>> get(DataFetchingEnvironment environment) throws Exception {
        List<Object> sources = environment.getSource();
        List<CompletableFuture<Object>> results = new ArrayList<>();
        for (Object source : sources) {

            DataFetchingEnvironment singleEnv = newDataFetchingEnvironment(environment)
                    .source(source).build();
            CompletableFuture<Object> cf = Async.toCompletableFuture(delegate.get(singleEnv));
            results.add(cf);
        }
        return Async.each(results);
    }
}
