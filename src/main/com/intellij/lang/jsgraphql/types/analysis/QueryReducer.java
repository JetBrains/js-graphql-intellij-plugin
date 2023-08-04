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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * Used by {@link QueryTraverser} to reduce the fields of a Document (or part of it) to a single value.
 * <p>
 * How this happens in detail (pre vs post-order for example) is defined by {@link QueryTraverser}.
 * <p>
 * See {@link QueryTraverser#reducePostOrder(QueryReducer, Object)} and {@link QueryTraverser#reducePreOrder(QueryReducer, Object)}
 */
@PublicApi
@FunctionalInterface
public interface QueryReducer<T> {

  /**
   * Called each time a field is visited.
   *
   * @param fieldEnvironment the environment to this call
   * @param acc              the previous result
   * @return the new result
   */
  T reduceField(QueryVisitorFieldEnvironment fieldEnvironment, T acc);
}
