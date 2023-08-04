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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Lets you traverse a {@link Node} tree.
 */
@PublicApi
public class NodeTraverser {


  private final Map<Class<?>, Object> rootVars;
  private final Function<? super Node, ? extends List<Node>> getChildren;

  public NodeTraverser(Map<Class<?>, Object> rootVars, Function<? super Node, ? extends List<Node>> getChildren) {
    this.rootVars = rootVars;
    this.getChildren = getChildren;
  }

  public NodeTraverser() {
    this(Collections.emptyMap(), Node::getChildren);
  }


  /**
   * depthFirst traversal with a enter/leave phase.
   *
   * @param nodeVisitor the visitor of the nodes
   * @param root        the root node
   * @return the accumulation result of this traversal
   */
  public Object depthFirst(NodeVisitor nodeVisitor, Node root) {
    return depthFirst(nodeVisitor, Collections.singleton(root));
  }

  /**
   * depthFirst traversal with a enter/leave phase.
   *
   * @param nodeVisitor the visitor of the nodes
   * @param roots       the root nodes
   * @return the accumulation result of this traversal
   */
  public Object depthFirst(NodeVisitor nodeVisitor, Collection<? extends Node> roots) {
    TraverserVisitor<Node> nodeTraverserVisitor = new TraverserVisitor<Node>() {

      @Override
      public TraversalControl enter(TraverserContext<Node> context) {
        return context.thisNode().accept(context, nodeVisitor);
      }

      @Override
      public TraversalControl leave(TraverserContext<Node> context) {
        return context.thisNode().accept(context, nodeVisitor);
      }
    };
    return doTraverse(roots, nodeTraverserVisitor);
  }

  /**
   * Version of {@link #preOrder(NodeVisitor, Collection)} with one root.
   *
   * @param nodeVisitor the visitor of the nodes
   * @param root        the root node
   * @return the accumulation result of this traversal
   */
  public Object preOrder(NodeVisitor nodeVisitor, Node root) {
    return preOrder(nodeVisitor, Collections.singleton(root));
  }

  /**
   * Pre-Order traversal: This is a specialized version of depthFirst with only the enter phase.
   *
   * @param nodeVisitor the visitor of the nodes
   * @param roots       the root nodes
   * @return the accumulation result of this traversal
   */
  public Object preOrder(NodeVisitor nodeVisitor, Collection<? extends Node> roots) {
    TraverserVisitor<Node> nodeTraverserVisitor = new TraverserVisitor<Node>() {

      @Override
      public TraversalControl enter(TraverserContext<Node> context) {
        return context.thisNode().accept(context, nodeVisitor);
      }

      @Override
      public TraversalControl leave(TraverserContext<Node> context) {
        return TraversalControl.CONTINUE;
      }
    };
    return doTraverse(roots, nodeTraverserVisitor);
  }

  /**
   * Version of {@link #postOrder(NodeVisitor, Collection)} with one root.
   *
   * @param nodeVisitor the visitor of the nodes
   * @param root        the root node
   * @return the accumulation result of this traversal
   */
  public Object postOrder(NodeVisitor nodeVisitor, Node root) {
    return postOrder(nodeVisitor, Collections.singleton(root));
  }

  /**
   * Post-Order traversal: This is a specialized version of depthFirst with only the leave phase.
   *
   * @param nodeVisitor the visitor of the nodes
   * @param roots       the root nodes
   * @return the accumulation result of this traversal
   */
  public Object postOrder(NodeVisitor nodeVisitor, Collection<? extends Node> roots) {
    TraverserVisitor<Node> nodeTraverserVisitor = new TraverserVisitor<Node>() {

      @Override
      public TraversalControl enter(TraverserContext<Node> context) {
        return TraversalControl.CONTINUE;
      }

      @Override
      public TraversalControl leave(TraverserContext<Node> context) {
        return context.thisNode().accept(context, nodeVisitor);
      }
    };
    return doTraverse(roots, nodeTraverserVisitor);
  }

  private Object doTraverse(Collection<? extends Node> roots, TraverserVisitor traverserVisitor) {
    Traverser<Node> nodeTraverser = Traverser.depthFirst(this.getChildren);
    nodeTraverser.rootVars(rootVars);
    return nodeTraverser.traverse(roots, traverserVisitor).getAccumulatedResult();
  }

  @SuppressWarnings("TypeParameterUnusedInFormals")
  public static <T> T oneVisitWithResult(Node node, NodeVisitor nodeVisitor) {
    DefaultTraverserContext<Node> context = DefaultTraverserContext.simple(node);
    node.accept(context, nodeVisitor);
    return context.getNewAccumulate();
  }
}
