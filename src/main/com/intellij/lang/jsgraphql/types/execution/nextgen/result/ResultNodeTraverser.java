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
package com.intellij.lang.jsgraphql.types.execution.nextgen.result;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.util.Traverser;
import com.intellij.lang.jsgraphql.types.util.TraverserVisitor;

import java.util.Collection;

@Internal
public class ResultNodeTraverser {

  private final Traverser<ExecutionResultNode> traverser;

  private ResultNodeTraverser(Traverser<ExecutionResultNode> traverser) {
    this.traverser = traverser;
  }

  public static ResultNodeTraverser depthFirst() {
    return new ResultNodeTraverser(Traverser.depthFirst(ExecutionResultNode::getChildren, null, null));
  }

  public void traverse(TraverserVisitor<ExecutionResultNode> visitor, ExecutionResultNode root) {
    traverser.traverse(root, visitor);
  }

  public void traverse(TraverserVisitor<ExecutionResultNode> visitor, Collection<? extends ExecutionResultNode> roots) {
    traverser.traverse(roots, visitor);
  }
}
