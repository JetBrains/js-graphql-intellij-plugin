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

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.*;
import java.util.function.Function;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@PublicApi
public class NodeZipper<T> {


  public enum ModificationType {
    REPLACE,
    DELETE,
    INSERT_AFTER,
    INSERT_BEFORE
  }

  private final T curNode;
  private final NodeAdapter<T> nodeAdapter;
  // reverse: the breadCrumbs start from curNode upwards
  private final List<Breadcrumb<T>> breadcrumbs;

  private final ModificationType modificationType;


  public NodeZipper(T curNode, List<Breadcrumb<T>> breadcrumbs, NodeAdapter<T> nodeAdapter) {
    this(curNode, breadcrumbs, nodeAdapter, ModificationType.REPLACE);
  }

  public NodeZipper(T curNode, List<Breadcrumb<T>> breadcrumbs, NodeAdapter<T> nodeAdapter, ModificationType modificationType) {
    this.curNode = assertNotNull(curNode);
    this.breadcrumbs = ImmutableList.copyOf(assertNotNull(breadcrumbs));
    this.nodeAdapter = nodeAdapter;
    this.modificationType = modificationType;
  }

  public ModificationType getModificationType() {
    return modificationType;
  }

  public T getCurNode() {
    return curNode;
  }

  public List<Breadcrumb<T>> getBreadcrumbs() {
    return breadcrumbs;
  }

  public T getParent() {
    return breadcrumbs.get(0).getNode();
  }

  public static <T> NodeZipper<T> rootZipper(T rootNode, NodeAdapter<T> nodeAdapter) {
    return new NodeZipper<T>(rootNode, new ArrayList<>(), nodeAdapter);
  }

  public NodeZipper<T> modifyNode(Function<T, T> transform) {
    return new NodeZipper<T>(transform.apply(curNode), breadcrumbs, nodeAdapter, this.modificationType);
  }

  public NodeZipper<T> deleteNode() {
    return new NodeZipper<T>(this.curNode, breadcrumbs, nodeAdapter, ModificationType.DELETE);
  }

  public NodeZipper<T> insertAfter(T toInsertAfter) {
    return new NodeZipper<T>(toInsertAfter, breadcrumbs, nodeAdapter, ModificationType.INSERT_AFTER);
  }

  public NodeZipper<T> insertBefore(T toInsertBefore) {
    return new NodeZipper<T>(toInsertBefore, breadcrumbs, nodeAdapter, ModificationType.INSERT_BEFORE);
  }

  public NodeZipper<T> withNewNode(T newNode) {
    return new NodeZipper<T>(newNode, breadcrumbs, nodeAdapter, this.modificationType);
  }

  public NodeZipper<T> moveUp() {
    T node = getParent();
    List<Breadcrumb<T>> newBreadcrumbs = breadcrumbs.subList(1, breadcrumbs.size());
    return new NodeZipper<>(node, newBreadcrumbs, nodeAdapter, this.modificationType);
  }


  /**
   * @return null if it is the root node and marked as deleted, otherwise never null
   */
  public T toRoot() {
    if (breadcrumbs.isEmpty()) {
      if (modificationType != ModificationType.DELETE) {
        return this.curNode;
      }
      else {
        return null;
      }
    }
    T curNode = this.curNode;

    Breadcrumb<T> firstBreadcrumb = breadcrumbs.get(0);
    Map<String, List<T>> childrenForParent = new HashMap<>(nodeAdapter.getNamedChildren(firstBreadcrumb.getNode()));
    NodeLocation locationInParent = firstBreadcrumb.getLocation();
    int ix = locationInParent.getIndex();
    String name = locationInParent.getName();
    List<T> childList = new ArrayList<>(childrenForParent.get(name));
    switch (modificationType) {
      case REPLACE -> childList.set(ix, curNode);
      case DELETE -> childList.remove(ix);
      case INSERT_BEFORE -> childList.add(ix, curNode);
      case INSERT_AFTER -> childList.add(ix + 1, curNode);
    }
    childrenForParent.put(name, childList);
    curNode = nodeAdapter.withNewChildren(firstBreadcrumb.getNode(), childrenForParent);
    if (breadcrumbs.size() == 1) {
      return curNode;
    }
    for (Breadcrumb<T> breadcrumb : breadcrumbs.subList(1, breadcrumbs.size())) {
      // just handle replace
      Map<String, List<T>> newChildren = new LinkedHashMap<>(nodeAdapter.getNamedChildren(breadcrumb.getNode()));
      final T newChild = curNode;
      NodeLocation location = breadcrumb.getLocation();
      List<T> list = new ArrayList<>(newChildren.get(location.getName()));
      list.set(location.getIndex(), newChild);
      newChildren.put(location.getName(), list);
      curNode = nodeAdapter.withNewChildren(breadcrumb.getNode(), newChildren);
    }
    return curNode;
  }

  @Override
  public String toString() {
    return "NodeZipper{" +
           "curNode=" + curNode +
           ", breadcrumbs.size=" + breadcrumbs.size() +
           ", modificationType=" + modificationType +
           '}';
  }
}
