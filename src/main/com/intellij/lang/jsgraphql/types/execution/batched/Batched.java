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

import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * When placed on {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher#get(DataFetchingEnvironment)}, indicates that this DataFetcher is batched.
 * This annotation must be used in conjunction with {@link BatchedExecutionStrategy}. Batching is valuable in many
 * situations, such as when a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} must make a network or file system request.
 * </p>
 * <p>
 * When a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} is batched, the {@link DataFetchingEnvironment#getSource()} method is
 * guaranteed to return a {@link java.util.List}.  The {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher#get(DataFetchingEnvironment)}
 * method MUST return a parallel {@link java.util.List} which is equivalent to running a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher}
 * over each input element individually.
 * </p>
 * <p>
 * Using the {@link Batched} annotation is equivalent to implementing {@link BatchedDataFetcher} instead of {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher}.
 * It is preferred to use the {@link Batched} annotation.
 * </p>
 * For example, the following two {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} objects are interchangeable if used with a
 * {@link BatchedExecutionStrategy}.
 * <pre>
 * <code>
 * new DataFetcher() {
 *   {@literal @}Override
 *   {@literal @}Batched
 *   public Object get(DataFetchingEnvironment environment) {
 *     {@literal List<String> retVal = new ArrayList<>();}
 *     {@literal for (String s: (List<String>) environment.getSource()) {}
 *       retVal.add(s + environment.getArgument("text"));
 *     }
 *     return retVal;
 *   }
 * }
 * </code>
 * </pre>
 * <pre>
 * <code>
 * new DataFetcher() {
 *   {@literal @}Override
 *   public Object get(DataFetchingEnvironment e) {
 *     return ((String)e.getSource()) + e.getArgument("text");
 *   }
 * }
 * </code>
 * </pre>
 *
 * @deprecated This has been deprecated in favour of using {@link com.intellij.lang.jsgraphql.types.execution.AsyncExecutionStrategy} and {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Deprecated
public @interface Batched {
}
