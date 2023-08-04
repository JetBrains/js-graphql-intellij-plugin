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

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.*;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.Assert.assertTrue;

/**
 * This represents a hierarchy from a graphql language node upwards to its
 * associated parent nodes.  For example a Directive can be on a InputValueDefinition
 * which can be on a Argument, which can be on a FieldDefinition which may be
 * on an ObjectTypeDefinition.
 */
@PublicApi
public class NodeParentTree<T extends Node> {

  private final T node;
  private final NodeParentTree<T> parent;
  private final ImmutableList<String> path;

  @Internal
  public NodeParentTree(Deque<T> nodeStack) {
    assertNotNull(nodeStack, () -> "You MUST have a non null stack of nodes");
    assertTrue(!nodeStack.isEmpty(), () -> "You MUST have a non empty stack of nodes");

    Deque<T> copy = new ArrayDeque<>(nodeStack);
    path = mkPath(copy);
    node = copy.pop();
    if (!copy.isEmpty()) {
      parent = new NodeParentTree<T>(copy);
    }
    else {
      parent = null;
    }
  }

  private ImmutableList<String> mkPath(Deque<T> copy) {
    return copy.stream()
      .filter(node1 -> node1 instanceof NamedNode)
      .map(node1 -> ((NamedNode)node1).getName())
      .collect(ImmutableList.toImmutableList());
  }


  /**
   * Returns the node represented by this info
   *
   * @return the node in play
   */
  public T getNode() {
    return node;
  }

  /**
   * @return a node MAY have an optional parent
   */
  public Optional<NodeParentTree<T>> getParentInfo() {
    return Optional.ofNullable(parent);
  }

  /**
   * @return a path of names for nodes thar are {@link com.intellij.lang.jsgraphql.types.language.NamedNode}s
   */
  public List<String> getPath() {
    return path;
  }

  /**
   * @return the tree as a list of T
   */
  public List<T> toList() {
    List<T> nodes = new ArrayList<>();
    nodes.add(node);
    Optional<NodeParentTree<T>> parentInfo = this.getParentInfo();
    while (parentInfo.isPresent()) {
      nodes.add(parentInfo.get().getNode());
      parentInfo = parentInfo.get().getParentInfo();
    }
    return nodes;
  }

  @Override
  public String toString() {
    return String.valueOf(node) +
           " - parent : " +
           parent;
  }
}
