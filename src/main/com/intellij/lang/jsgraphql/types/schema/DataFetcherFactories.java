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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * A helper for {@link com.intellij.lang.jsgraphql.types.schema.DataFetcherFactory}
 */
@PublicApi
public class DataFetcherFactories {

    /**
     * Creates a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcherFactory} that always returns the provided {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher}
     *
     * @param dataFetcher the data fetcher to always return
     * @param <T>         the type of the data fetcher
     *
     * @return a data fetcher factory that always returns the provided data fetcher
     */
    public static <T> DataFetcherFactory<T> useDataFetcher(DataFetcher<T> dataFetcher) {
        return fieldDefinition -> dataFetcher;
    }

    /**
     * This helper function allows you to wrap an existing data fetcher and map the value once it completes.  It helps you handle
     * values that might be {@link  CompletionStage} returned values as well as plain old objects.
     *
     * @param delegateDataFetcher the original data fetcher that is present on a {@link com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition} say
     * @param mapFunction         the bi function to apply to the original value
     *
     * @return a new data fetcher that wraps the provided data fetcher
     */
    public static DataFetcher wrapDataFetcher(DataFetcher delegateDataFetcher, BiFunction<DataFetchingEnvironment, Object, Object> mapFunction) {
        return environment -> {
            Object value = delegateDataFetcher.get(environment);
            if (value instanceof CompletionStage) {
                //noinspection unchecked
                return ((CompletionStage<Object>) value).thenApply(v -> mapFunction.apply(environment, v));
            } else {
                return mapFunction.apply(environment, value);
            }
        };
    }

}
