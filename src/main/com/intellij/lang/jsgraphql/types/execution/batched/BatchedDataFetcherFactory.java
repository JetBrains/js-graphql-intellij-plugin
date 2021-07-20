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
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;
import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;

import java.lang.reflect.Method;

/**
 * Produces a BatchedDataFetcher for a given DataFetcher.
 * If that fetcher is already a BatchedDataFetcher we return it.
 * If that fetcher's get method is annotated @Batched then we delegate to it directly.
 * Otherwise we wrap the fetcher in a BatchedDataFetcher that iterates over the sources and invokes the delegate
 * on each source. Note that this forgoes any performance benefits of batching,
 * so regular DataFetchers should normally only be used if they are in-memory.
 *
 * @deprecated This has been deprecated in favour of using {@link com.intellij.lang.jsgraphql.types.execution.AsyncExecutionStrategy} and {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation}
 */
@Deprecated
@PublicApi
public class BatchedDataFetcherFactory {
    public BatchedDataFetcher create(final DataFetcher supplied) {
        if (supplied instanceof BatchedDataFetcher) {
            return (BatchedDataFetcher) supplied;
        }
        try {
            Method getMethod = supplied.getClass().getMethod("get", DataFetchingEnvironment.class);
            Batched batched = getMethod.getAnnotation(Batched.class);
            if (batched != null) {
                return supplied::get;
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        return new UnbatchedDataFetcher(supplied);
    }
}
