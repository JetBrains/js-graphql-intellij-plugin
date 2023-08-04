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
package com.intellij.lang.jsgraphql.types.util;

import com.intellij.lang.jsgraphql.types.Internal;

@Internal
public interface TraverserVisitor<T> {

  /**
   * @param context the context in place
   * @return Any allowed control value
   */
  TraversalControl enter(TraverserContext<T> context);

  /**
   * @param context the context in place
   * @return Only Continue or Quit allowed
   */
  TraversalControl leave(TraverserContext<T> context);

  /**
   * This method is called when a node was already visited before.
   * <p>
   * This can happen for two reasons:
   * 1. There is a cycle.
   * 2. A node has more than one parent. This means the structure is not a tree but a graph.
   *
   * @param context the context in place
   * @return Only Continue or Quit allowed
   */
  default TraversalControl backRef(TraverserContext<T> context) {
    return TraversalControl.CONTINUE;
  }
}
