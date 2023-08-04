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
package com.intellij.lang.jsgraphql.types.execution.instrumentation;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A simple implementation of {@link InstrumentationContext}
 */
@PublicApi
public class SimpleInstrumentationContext<T> implements InstrumentationContext<T> {

  private static final InstrumentationContext<Object> NO_OP = new SimpleInstrumentationContext<>();

  /**
   * A context that does nothing
   *
   * @param <T> the type needed
   * @return a context that does nothing
   */
  @SuppressWarnings("unchecked")
  public static <T> InstrumentationContext<T> noOp() {
    return (InstrumentationContext<T>)NO_OP;
  }

  private final BiConsumer<T, Throwable> codeToRunOnComplete;
  private final Consumer<CompletableFuture<T>> codeToRunOnDispatch;

  public SimpleInstrumentationContext() {
    this(null, null);
  }

  private SimpleInstrumentationContext(Consumer<CompletableFuture<T>> codeToRunOnDispatch, BiConsumer<T, Throwable> codeToRunOnComplete) {
    this.codeToRunOnComplete = codeToRunOnComplete;
    this.codeToRunOnDispatch = codeToRunOnDispatch;
  }

  @Override
  public void onDispatched(CompletableFuture<T> result) {
    if (codeToRunOnDispatch != null) {
      codeToRunOnDispatch.accept(result);
    }
  }

  @Override
  public void onCompleted(T result, Throwable t) {
    if (codeToRunOnComplete != null) {
      codeToRunOnComplete.accept(result, t);
    }
  }

  /**
   * Allows for the more fluent away to return an instrumentation context that runs the specified
   * code on instrumentation step dispatch.
   *
   * @param codeToRun the code to run on dispatch
   * @param <U>       the generic type
   * @return an instrumentation context
   */
  public static <U> SimpleInstrumentationContext<U> whenDispatched(Consumer<CompletableFuture<U>> codeToRun) {
    return new SimpleInstrumentationContext<>(codeToRun, null);
  }

  /**
   * Allows for the more fluent away to return an instrumentation context that runs the specified
   * code on instrumentation step completion.
   *
   * @param codeToRun the code to run on completion
   * @param <U>       the generic type
   * @return an instrumentation context
   */
  public static <U> SimpleInstrumentationContext<U> whenCompleted(BiConsumer<U, Throwable> codeToRun) {
    return new SimpleInstrumentationContext<>(null, codeToRun);
  }
}
