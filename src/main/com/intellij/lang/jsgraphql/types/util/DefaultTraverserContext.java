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
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.intellij.lang.jsgraphql.types.Assert.*;

@Internal
public class DefaultTraverserContext<T> implements TraverserContext<T> {

  private final T curNode;
  private T newNode;
  private boolean nodeDeleted;

  private final TraverserContext<T> parent;
  private final Set<T> visited;
  private final Map<Class<?>, Object> vars;
  private final Object sharedContextData;

  private Object newAccValue;
  private boolean hasNewAccValue;
  private Object curAccValue;
  private final NodeLocation location;
  private final boolean isRootContext;
  private boolean parallel;
  private Map<String, List<TraverserContext<T>>> children;
  private Phase phase;
  private final List<Breadcrumb<T>> breadcrumbs;

  public DefaultTraverserContext(T curNode,
                                 TraverserContext<T> parent,
                                 Set<T> visited,
                                 Map<Class<?>, Object> vars,
                                 Object sharedContextData,
                                 NodeLocation location,
                                 boolean isRootContext,
                                 boolean parallel) {
    this.curNode = curNode;
    this.parent = parent;
    this.visited = visited;
    this.vars = vars;
    this.sharedContextData = sharedContextData;
    this.location = location;
    this.isRootContext = isRootContext;
    this.parallel = parallel;

    if (parent == null || parent.isRootContext()) {
      this.breadcrumbs = ImmutableKit.emptyList();
    }
    else {
      List<Breadcrumb<T>> breadcrumbs = new ArrayList<>(parent.getBreadcrumbs().size() + 1);
      breadcrumbs.add(new Breadcrumb<>(this.parent.thisNode(), this.location));
      breadcrumbs.addAll(parent.getBreadcrumbs());
      this.breadcrumbs = ImmutableList.copyOf(breadcrumbs);
    }
  }

  public static <T> DefaultTraverserContext<T> dummy() {
    return new DefaultTraverserContext<>(null, null, null, null, null, null, true, false);
  }

  public static <T> DefaultTraverserContext<T> simple(T node) {
    return new DefaultTraverserContext<>(node, null, null, null, null, null, true, false);
  }

  @Override
  public T thisNode() {
    assertFalse(this.nodeDeleted, () -> "node is deleted");
    if (newNode != null) {
      return newNode;
    }
    return curNode;
  }

  @Override
  public T originalThisNode() {
    return curNode;
  }

  @Override
  public void changeNode(T newNode) {
    assertNotNull(newNode);
    assertFalse(this.nodeDeleted, () -> "node is deleted");
    this.newNode = newNode;
  }


  @Override
  public void deleteNode() {
    assertNull(this.newNode, () -> "node is already changed");
    assertFalse(this.nodeDeleted, () -> "node is already deleted");
    this.nodeDeleted = true;
  }

  @Override
  public boolean isDeleted() {
    return this.nodeDeleted;
  }

  @Override
  public boolean isChanged() {
    return this.newNode != null;
  }


  @Override
  public TraverserContext<T> getParentContext() {
    return parent;
  }

  @Override
  public List<T> getParentNodes() {
    List<T> result = new ArrayList<>();
    TraverserContext<T> curContext = parent;
    while (!curContext.isRootContext()) {
      result.add(curContext.thisNode());
      curContext = curContext.getParentContext();
    }
    return result;
  }

  @Override
  public List<Breadcrumb<T>> getBreadcrumbs() {
    return breadcrumbs;
  }

  @Override
  public T getParentNode() {
    if (parent == null) {
      return null;
    }
    return parent.thisNode();
  }

  @Override
  public Set<T> visitedNodes() {
    return visited;
  }

  @Override
  public boolean isVisited() {
    return visited.contains(curNode);
  }

  @Override
  public <S> S getVar(Class<? super S> key) {
    return (S)key.cast(vars.get(key));
  }

  @Override
  public <S> TraverserContext<T> setVar(Class<? super S> key, S value) {
    vars.put(key, value);
    return this;
  }

  @Override
  public void setAccumulate(Object accumulate) {
    hasNewAccValue = true;
    newAccValue = accumulate;
  }

  @Override
  public <U> U getNewAccumulate() {
    if (hasNewAccValue) {
      return (U)newAccValue;
    }
    else {
      return (U)curAccValue;
    }
  }

  @Override
  public <U> U getCurrentAccumulate() {
    return (U)curAccValue;
  }


  @Override
  public Object getSharedContextData() {
    return sharedContextData;
  }

  /*
   * PRIVATE: Used by {@link Traverser}
   */
  void setCurAccValue(Object curAccValue) {
    hasNewAccValue = false;
    this.curAccValue = curAccValue;
  }

  @Override
  public NodeLocation getLocation() {
    return location;
  }

  @Override
  public boolean isRootContext() {
    return isRootContext;
  }

  @Override
  public <S> S getVarFromParents(Class<? super S> key) {
    TraverserContext<T> curContext = parent;
    while (curContext != null) {
      S var = curContext.getVar(key);
      if (var != null) {
        return var;
      }
      curContext = curContext.getParentContext();
    }
    return null;
  }

  /*
   * PRIVATE: Used by {@link Traverser}
   */
  void setChildrenContexts(Map<String, List<TraverserContext<T>>> children) {
    assertTrue(this.children == null, () -> "children already set");
    this.children = children;
  }


  @Override
  public Map<String, List<TraverserContext<T>>> getChildrenContexts() {
    assertNotNull(children, () -> "children not available");
    return children;
  }

  /*
   * PRIVATE: Used by {@link Traverser}
   */
  void setPhase(Phase phase) {
    this.phase = phase;
  }

  @Override
  public Phase getPhase() {
    return phase;
  }

  @Override
  public boolean isParallel() {
    return parallel;
  }
}
