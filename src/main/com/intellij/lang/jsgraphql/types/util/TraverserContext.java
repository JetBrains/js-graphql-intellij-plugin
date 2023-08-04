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

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Traversal context.
 * <p>
 * It is used as providing context for traversing, but also for returning an accumulate value. ({@link #setAccumulate(Object)}
 * <p>
 * There is always a "fake" root context with null node, null parent, null position. See {@link #isRootContext()}
 *
 * @param <T> type of tree node
 */
@PublicApi
public interface TraverserContext<T> {

  enum Phase {
    LEAVE,
    ENTER,
    BACKREF
  }

  /**
   * Returns current node being visited.
   * Special cases:
   * It is null for the root context and it is the changed node after {@link #changeNode(Object)} is called.
   * Throws Exception if the node is deleted.
   *
   * @return current node traverser is visiting.
   * @throws graphql.AssertException if the current node is deleted
   */
  T thisNode();

  /**
   * Returns the original, unchanged, not deleted Node.
   *
   * @return the original node
   */
  T originalThisNode();

  /**
   * Change the current node to the provided node. Only applicable in enter.
   * <p>
   * Useful when the tree should be changed while traversing.
   * <p>
   * Also: changing a node makes only a difference when it has different children than the current one.
   *
   * @param newNode the new Node
   */
  void changeNode(T newNode);

  /**
   * Deletes the current node.
   */
  void deleteNode();

  /**
   * @return true if the current node is deleted (by calling {@link #deleteNode()}
   */
  boolean isDeleted();

  /**
   * @return true if the current node is changed (by calling {@link #changeNode(Object)}
   */
  boolean isChanged();

  /**
   * Returns parent context.
   * Effectively organizes Context objects in a linked list so
   * by following these links one could obtain
   * the current path as well as the variables {@link #getVar(Class) }
   * stored in every parent context.
   *
   * @return context associated with the node parent
   */
  TraverserContext<T> getParentContext();

  /**
   * The list of parent nodes starting from the current parent.
   *
   * @return list of parent nodes
   */
  List<T> getParentNodes();

  /**
   * The parent node.
   *
   * @return The parent node.
   */
  T getParentNode();


  /**
   * The exact location of this node inside the tree as a list of {@link Breadcrumb}
   *
   * @return list of breadcrumbs. the first element is the location inside the parent.
   */
  List<Breadcrumb<T>> getBreadcrumbs();

  /**
   * The location of the current node regarding to the parent node.
   *
   * @return the position or null if this node is a root node
   */
  NodeLocation getLocation();

  /**
   * Informs that the current node has been already "visited"
   *
   * @return {@code true} if a node had been already visited
   */
  boolean isVisited();

  /**
   * Obtains all visited nodes and values received by the {@link TraverserVisitor#enter(com.intellij.lang.jsgraphql.types.util.TraverserContext) }
   * method
   *
   * @return a map containg all nodes visited and values passed when visiting nodes for the first time
   */
  Set<T> visitedNodes();

  /**
   * Obtains a context local variable
   *
   * @param <S> type of the variable
   * @param key key to lookup the variable value
   * @return a variable value or {@code null}
   */
  <S> S getVar(Class<? super S> key);

  /**
   * Searches for a context variable starting from the parent
   * up the hierarchy of contexts until the first variable is found.
   *
   * @param <S> type of the variable
   * @param key key to lookup the variable value
   * @return a variable value or {@code null}
   */
  <S> S getVarFromParents(Class<? super S> key);

  /**
   * Stores a variable in the context
   *
   * @param <S>   type of a varable
   * @param key   key to create bindings for the variable
   * @param value value of variable
   * @return this context to allow operations chaining
   */
  <S> TraverserContext<T> setVar(Class<? super S> key, S value);


  /**
   * Sets the new accumulate value.
   * <p>
   * Can be retrieved by {@link #getNewAccumulate()}
   *
   * @param accumulate to set
   */
  void setAccumulate(Object accumulate);

  /**
   * The new accumulate value, previously set by {@link #setAccumulate(Object)}
   * or {@link #getCurrentAccumulate()} if {@link #setAccumulate(Object)} not invoked.
   *
   * @param <U> and me
   * @return the new accumulate value
   */
  <U> U getNewAccumulate();

  /**
   * The current accumulate value used as "input" for the current step.
   *
   * @param <U> and me
   * @return the current accumulate value
   */
  <U> U getCurrentAccumulate();

  /**
   * Used to share something across all TraverserContext.
   *
   * @param <U> and me
   * @return contextData
   */
  <U> U getSharedContextData();

  /**
   * Returns true for the root context, which doesn't have a node or a position.
   *
   * @return true for the root context, otherwise false
   */
  boolean isRootContext();

  /**
   * In case of leave returns the children contexts, which have already been visited.
   *
   * @return the children contexts. If the childs are a simple list the key is null.
   */
  Map<String, List<TraverserContext<T>>> getChildrenContexts();

  /**
   * @return the phase in which the node visits currently happens (Enter,Leave or BackRef)
   */
  Phase getPhase();

  /**
   * @return true if the traversing happens in parallel (multi threaded) or not.
   */
  boolean isParallel();
}
