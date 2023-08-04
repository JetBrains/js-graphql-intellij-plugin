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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * A modifier type that indicates the underlying data fetcher is run asynchronously
 */
@PublicApi
public class AsyncDataFetcher<T> implements DataFetcher<CompletableFuture<T>> {

  /**
   * A factory method for creating asynchronous data fetchers so that when used with
   * static imports allows more readable code such as:
   * <p>
   * {@code .dataFetcher(async(fooDataFetcher))}
   * <p>
   * By default this will run in the {@link ForkJoinPool#commonPool()}. You can set
   * your own {@link Executor} with {@link #async(DataFetcher, Executor)}
   *
   * @param wrappedDataFetcher the data fetcher to run asynchronously
   * @param <T>                the type of data
   * @return a {@link DataFetcher} that will run the wrappedDataFetcher asynchronously
   */
  public static <T> AsyncDataFetcher<T> async(DataFetcher<T> wrappedDataFetcher) {
    return new AsyncDataFetcher<>(wrappedDataFetcher);
  }

  /**
   * A factory method for creating asynchronous data fetchers and setting the
   * {@link Executor} they run in so that when used with static imports allows
   * more readable code such as:
   * <p>
   * {@code .dataFetcher(async(fooDataFetcher, fooExecutor))}
   *
   * @param wrappedDataFetcher the data fetcher to run asynchronously
   * @param executor           the executor to run the asynchronous data fetcher in
   * @param <T>                the type of data
   * @return a {@link DataFetcher} that will run the wrappedDataFetcher asynchronously in
   * the given {@link Executor}
   */
  public static <T> AsyncDataFetcher<T> async(DataFetcher<T> wrappedDataFetcher, Executor executor) {
    return new AsyncDataFetcher<>(wrappedDataFetcher, executor);
  }

  private final DataFetcher<T> wrappedDataFetcher;
  private final Executor executor;

  public AsyncDataFetcher(DataFetcher<T> wrappedDataFetcher) {
    this(wrappedDataFetcher, ForkJoinPool.commonPool());
  }

  public AsyncDataFetcher(DataFetcher<T> wrappedDataFetcher, Executor executor) {
    this.wrappedDataFetcher = assertNotNull(wrappedDataFetcher, () -> "wrappedDataFetcher can't be null");
    this.executor = assertNotNull(executor, () -> "executor can't be null");
  }

  @Override
  public CompletableFuture<T> get(DataFetchingEnvironment environment) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return wrappedDataFetcher.get(environment);
      }
      catch (Exception e) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException)e;
        }
        else {
          throw new RuntimeException(e);
        }
      }
    }, executor);
  }
}
