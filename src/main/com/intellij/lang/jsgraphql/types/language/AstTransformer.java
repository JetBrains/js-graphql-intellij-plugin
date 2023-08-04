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
package com.intellij.lang.jsgraphql.types.language;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.util.*;

import java.util.concurrent.ForkJoinPool;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.language.AstNodeAdapter.AST_NODE_ADAPTER;

/**
 * Allows for an easy way to "manipulate" the immutable Ast by changing specific nodes and getting back a new Ast
 * containing the changed nodes while everything else is the same.
 */
@PublicApi
public class AstTransformer {


  public Node transform(Node root, NodeVisitor nodeVisitor) {
    assertNotNull(root);
    assertNotNull(nodeVisitor);

    TraverserVisitor<Node> traverserVisitor = new TraverserVisitor<Node>() {
      @Override
      public TraversalControl enter(TraverserContext<Node> context) {
        return context.thisNode().accept(context, nodeVisitor);
      }

      @Override
      public TraversalControl leave(TraverserContext<Node> context) {
        return TraversalControl.CONTINUE;
      }
    };

    TreeTransformer<Node> treeTransformer = new TreeTransformer<>(AST_NODE_ADAPTER);
    return treeTransformer.transform(root, traverserVisitor);
  }

  public Node transformParallel(Node root, NodeVisitor nodeVisitor) {
    return transformParallel(root, nodeVisitor, ForkJoinPool.commonPool());
  }

  public Node transformParallel(Node root, NodeVisitor nodeVisitor, ForkJoinPool forkJoinPool) {
    assertNotNull(root);
    assertNotNull(nodeVisitor);

    TraverserVisitor<Node> traverserVisitor = new TraverserVisitorStub<Node>() {
      @Override
      public TraversalControl enter(TraverserContext<Node> context) {
        return context.thisNode().accept(context, nodeVisitor);
      }
    };

    TreeParallelTransformer<Node> treeParallelTransformer = TreeParallelTransformer.parallelTransformer(AST_NODE_ADAPTER, forkJoinPool);
    return treeParallelTransformer.transform(root, traverserVisitor);
  }
}
