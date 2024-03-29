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

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Internal
@SuppressWarnings("FutureReturnValueIgnored")
public class Async {

  @FunctionalInterface
  public interface CFFactory<T, U> {
    CompletableFuture<U> apply(T input, int index, List<U> previousResults);
  }

  public static <U> CompletableFuture<List<U>> each(List<CompletableFuture<U>> futures) {
    CompletableFuture<List<U>> overallResult = new CompletableFuture<>();

    @SuppressWarnings("unchecked")
    CompletableFuture<U>[] arrayOfFutures = futures.toArray(new CompletableFuture[0]);
    CompletableFuture
      .allOf(arrayOfFutures)
      .whenComplete((ignored, exception) -> {
        if (exception != null) {
          overallResult.completeExceptionally(exception);
          return;
        }
        List<U> results = new ArrayList<>(arrayOfFutures.length);
        for (CompletableFuture<U> future : arrayOfFutures) {
          results.add(future.join());
        }
        overallResult.complete(results);
      });
    return overallResult;
  }

  public static <T, U> CompletableFuture<List<U>> each(Collection<T> list, BiFunction<T, Integer, CompletableFuture<U>> cfFactory) {
    List<CompletableFuture<U>> futures = new ArrayList<>(list.size());
    int index = 0;
    for (T t : list) {
      CompletableFuture<U> cf;
      try {
        cf = cfFactory.apply(t, index++);
        Assert.assertNotNull(cf, () -> "cfFactory must return a non null value");
      }
      catch (Exception e) {
        cf = new CompletableFuture<>();
        // Async.each makes sure that it is not a CompletionException inside a CompletionException
        cf.completeExceptionally(new CompletionException(e));
      }
      futures.add(cf);
    }
    return each(futures);
  }

  public static <T, U> CompletableFuture<List<U>> eachSequentially(Iterable<T> list, CFFactory<T, U> cfFactory) {
    CompletableFuture<List<U>> result = new CompletableFuture<>();
    eachSequentiallyImpl(list.iterator(), cfFactory, 0, new ArrayList<>(), result);
    return result;
  }

  private static <T, U> void eachSequentiallyImpl(Iterator<T> iterator,
                                                  CFFactory<T, U> cfFactory,
                                                  int index,
                                                  List<U> tmpResult,
                                                  CompletableFuture<List<U>> overallResult) {
    if (!iterator.hasNext()) {
      overallResult.complete(tmpResult);
      return;
    }
    CompletableFuture<U> cf;
    try {
      cf = cfFactory.apply(iterator.next(), index, tmpResult);
      Assert.assertNotNull(cf, () -> "cfFactory must return a non null value");
    }
    catch (Exception e) {
      cf = new CompletableFuture<>();
      cf.completeExceptionally(new CompletionException(e));
    }
    cf.whenComplete((cfResult, exception) -> {
      if (exception != null) {
        overallResult.completeExceptionally(exception);
        return;
      }
      tmpResult.add(cfResult);
      eachSequentiallyImpl(iterator, cfFactory, index + 1, tmpResult, overallResult);
    });
  }


  /**
   * Turns an object T into a CompletableFuture if its not already
   *
   * @param t   - the object to check
   * @param <T> for two
   * @return a CompletableFuture
   */
  public static <T> CompletableFuture<T> toCompletableFuture(T t) {
    if (t instanceof CompletionStage) {
      //noinspection unchecked
      return ((CompletionStage<T>)t).toCompletableFuture();
    }
    else {
      return CompletableFuture.completedFuture(t);
    }
  }

  public static <T> CompletableFuture<T> tryCatch(Supplier<CompletableFuture<T>> supplier) {
    try {
      return supplier.get();
    }
    catch (Exception e) {
      CompletableFuture<T> result = new CompletableFuture<>();
      result.completeExceptionally(e);
      return result;
    }
  }

  public static <T> CompletableFuture<T> exceptionallyCompletedFuture(Throwable exception) {
    CompletableFuture<T> result = new CompletableFuture<>();
    result.completeExceptionally(exception);
    return result;
  }

  public static <U, T> CompletableFuture<List<U>> flatMap(List<T> inputs, Function<T, CompletableFuture<U>> mapper) {
    List<CompletableFuture<U>> collect = ImmutableKit.map(inputs, mapper);
    return Async.each(collect);
  }

  public static <U, T> CompletableFuture<List<U>> map(CompletableFuture<List<T>> values, Function<T, U> mapper) {
    return values.thenApply(list -> ImmutableKit.map(list, mapper));
  }

  public static <U, T> List<CompletableFuture<U>> map(List<CompletableFuture<T>> values, Function<T, U> mapper) {
    return ImmutableKit.map(values, cf -> cf.thenApply(mapper));
  }

  public static <U, T> List<CompletableFuture<U>> mapCompose(List<CompletableFuture<T>> values, Function<T, CompletableFuture<U>> mapper) {
    return ImmutableKit.map(values, cf -> cf.thenCompose(mapper));
  }
}
