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

import com.intellij.lang.jsgraphql.types.PublicSpi;

import java.util.concurrent.CompletableFuture;

/**
 * When a {@link Instrumentation}.'beginXXX()' method is called then it must return a non null InstrumentationContext
 * that will be invoked when the step is first dispatched and then when it completes.  Sometimes this is effectively the same time
 * whereas at other times its when an asynchronous {@link CompletableFuture} completes.
 * <p>
 * This pattern of construction of an object then call back is intended to allow "timers" to be created that can instrument what has
 * just happened or "loggers" to be called to record what has happened.
 */
@PublicSpi
public interface InstrumentationContext<T> {

  /**
   * This is invoked when the instrumentation step is initially dispatched
   *
   * @param result the result of the step as a completable future
   */
  void onDispatched(CompletableFuture<T> result);

  /**
   * This is invoked when the instrumentation step is fully completed
   *
   * @param result the result of the step (which may be null)
   * @param t      this exception will be non null if an exception was thrown during the step
   */
  void onCompleted(T result, Throwable t);
}
