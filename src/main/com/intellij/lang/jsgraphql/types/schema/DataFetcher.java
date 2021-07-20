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


import com.intellij.lang.jsgraphql.types.PublicSpi;

/**
 * A data fetcher is responsible for returning a data value back for a given graphql field.  The graphql engine
 * uses data fetchers to resolve / fetch a logical field into a runtime object that will be sent back as part
 * of the overall graphql {@link com.intellij.lang.jsgraphql.types.ExecutionResult}
 *
 * In other implementations, these are sometimes called "Resolvers" or "Field Resolvers", because that is there function,
 * they resolve a logical graphql field into an actual data value.
 *
 * @param <T> the type of object returned. May also be wrapped in a {@link com.intellij.lang.jsgraphql.types.execution.DataFetcherResult}
 */
@PublicSpi
public interface DataFetcher<T> {

    /**
     * This is called by the graphql engine to fetch the value.  The {@link com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment} is a composite
     * context object that tells you all you need to know about how to fetch a data value in graphql type terms.
     *
     * @param environment this is the data fetching environment which contains all the context you need to fetch a value
     *
     * @return a value of type T. May be wrapped in a {@link com.intellij.lang.jsgraphql.types.execution.DataFetcherResult}
     *
     * @throws Exception to relieve the implementations from having to wrap checked exceptions. Any exception thrown
     *                   from a {@code DataFetcher} will eventually be handled by the registered {@link com.intellij.lang.jsgraphql.types.execution.DataFetcherExceptionHandler}
     *                   and the related field will have a value of {@code null} in the result.
     */
    T get(DataFetchingEnvironment environment) throws Exception;


}
