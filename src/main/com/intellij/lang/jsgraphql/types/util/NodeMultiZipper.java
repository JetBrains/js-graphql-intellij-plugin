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

import static com.intellij.lang.jsgraphql.types.Assert.*;
import static com.intellij.lang.jsgraphql.types.util.NodeZipper.ModificationType.REPLACE;

@PublicApi
public class NodeMultiZipper<T> {

  private final T commonRoot;
  private final ImmutableList<NodeZipper<T>> zippers;
  private final NodeAdapter<T> nodeAdapter;

  public NodeMultiZipper(T commonRoot, List<NodeZipper<T>> zippers, NodeAdapter<T> nodeAdapter) {
    this.commonRoot = assertNotNull(commonRoot);
    this.zippers = ImmutableList.copyOf(zippers);
    this.nodeAdapter = nodeAdapter;
  }

  /*
   * constructor without defensive copy of the zippers
   */
  private NodeMultiZipper(T commonRoot, List<NodeZipper<T>> zippers, NodeAdapter<T> nodeAdapter, Object dummy) {
    this.commonRoot = assertNotNull(commonRoot);
    this.zippers = ImmutableList.copyOf(zippers);
    this.nodeAdapter = nodeAdapter;
  }

  /*
   * special factory method which doesn't copy the zippers list and trusts that the zippers list is not modified outside
   */
  public static <T> NodeMultiZipper<T> newNodeMultiZipperTrusted(T commonRoot, List<NodeZipper<T>> zippers, NodeAdapter<T> nodeAdapter) {
    return new NodeMultiZipper<>(commonRoot, zippers, nodeAdapter, null);
  }

  /**
   * @return can be null if the root node is marked as deleted
   */
  public T toRootNode() {
    if (zippers.size() == 0) {
      return commonRoot;
    }

    // we want to preserve the order here
    Set<NodeZipper<T>> curZippers = new LinkedHashSet<>(zippers);
    while (curZippers.size() > 1) {

      List<NodeZipper<T>> deepestZippers = getDeepestZippers(curZippers);
      Map<T, ImmutableList<NodeZipper<T>>> sameParent = zipperWithSameParent(deepestZippers);

      List<NodeZipper<T>> newZippers = new ArrayList<>();
      Map<T, NodeZipper<T>> zipperByNode = FpKit.groupingByUniqueKey(curZippers, NodeZipper::getCurNode);
      for (Map.Entry<T, ImmutableList<NodeZipper<T>>> entry : sameParent.entrySet()) {
        NodeZipper<T> newZipper = moveUp(entry.getKey(), entry.getValue());
        Optional<NodeZipper<T>> zipperToBeReplaced = Optional.ofNullable(zipperByNode.get(entry.getKey()));
        zipperToBeReplaced.ifPresent(curZippers::remove);
        newZippers.add(newZipper);
      }
      curZippers.removeAll(deepestZippers);
      curZippers.addAll(newZippers);
    }
    assertTrue(curZippers.size() == 1, () -> "unexpected state: all zippers must share the same root node");
    return curZippers.iterator().next().toRoot();
  }

  public T getCommonRoot() {
    return commonRoot;
  }

  public List<NodeZipper<T>> getZippers() {
    return zippers;
  }

  public int size() {
    return zippers.size();
  }

  public NodeZipper<T> getZipperForNode(T node) {
    return FpKit.findOneOrNull(zippers, zipper -> zipper.getCurNode() == node);
  }

  public NodeMultiZipper<T> withReplacedZippers(List<NodeZipper<T>> zippers) {
    return new NodeMultiZipper<>(commonRoot, zippers, this.nodeAdapter);
  }


  public NodeMultiZipper<T> withNewZipper(NodeZipper<T> newZipper) {
    List<NodeZipper<T>> newZippers = new ArrayList<>(zippers);
    newZippers.add(newZipper);
    return new NodeMultiZipper<>(commonRoot, newZippers, this.nodeAdapter);
  }

  public NodeMultiZipper<T> withReplacedZipper(NodeZipper<T> oldZipper, NodeZipper<T> newZipper) {
    int index = zippers.indexOf(oldZipper);
    assertTrue(index >= 0, () -> "oldZipper not found");
    List<NodeZipper<T>> newZippers = new ArrayList<>(zippers);
    newZippers.set(index, newZipper);
    return new NodeMultiZipper<>(commonRoot, newZippers, this.nodeAdapter);
  }

  public NodeMultiZipper<T> withReplacedZipperForNode(T currentNode, T newNode) {
    int index = FpKit.findIndex(zippers, zipper -> zipper.getCurNode() == currentNode);
    assertTrue(index >= 0, () -> "No current zipper found for provided node");
    NodeZipper<T> newZipper = zippers.get(index).withNewNode(newNode);
    List<NodeZipper<T>> newZippers = new ArrayList<>(zippers);
    newZippers.set(index, newZipper);
    return new NodeMultiZipper<>(commonRoot, newZippers, this.nodeAdapter);
  }


  private List<NodeZipper<T>> getDeepestZippers(Set<NodeZipper<T>> zippers) {
    Map<Integer, ImmutableList<NodeZipper<T>>> grouped = FpKit.groupingBy(zippers, astZipper -> astZipper.getBreadcrumbs().size());

    Integer maxLevel = Collections.max(grouped.keySet());
    return grouped.get(maxLevel);
  }

  private NodeZipper<T> moveUp(T parent, List<NodeZipper<T>> sameParent) {
    assertNotEmpty(sameParent, () -> "expected at least one zipper");

    Map<String, List<T>> childrenMap = new HashMap<>(nodeAdapter.getNamedChildren(parent));
    Map<String, Integer> indexCorrection = new HashMap<>();

    sameParent = new ArrayList<>(sameParent);
    sameParent.sort((zipper1, zipper2) -> {
      int index1 = zipper1.getBreadcrumbs().get(0).getLocation().getIndex();
      int index2 = zipper2.getBreadcrumbs().get(0).getLocation().getIndex();
      if (index1 != index2) {
        return Integer.compare(index1, index2);
      }
      NodeZipper.ModificationType modificationType1 = zipper1.getModificationType();
      NodeZipper.ModificationType modificationType2 = zipper2.getModificationType();

      // same index can never be deleted and changed at the same time

      if (modificationType1 == modificationType2) {
        return 0;
      }

      // always first replacing the node
      if (modificationType1 == REPLACE) {
        return -1;
      }
      // and then INSERT_BEFORE before INSERT_AFTER
      return modificationType1 == NodeZipper.ModificationType.INSERT_BEFORE ? -1 : 1;
    });

    for (NodeZipper<T> zipper : sameParent) {
      NodeLocation location = zipper.getBreadcrumbs().get(0).getLocation();
      Integer ixDiff = indexCorrection.getOrDefault(location.getName(), 0);
      int ix = location.getIndex() + ixDiff;
      String name = location.getName();
      List<T> childList = new ArrayList<>(childrenMap.get(name));
      switch (zipper.getModificationType()) {
        case REPLACE:
          childList.set(ix, zipper.getCurNode());
          break;
        case DELETE:
          childList.remove(ix);
          indexCorrection.put(name, ixDiff - 1);
          break;
        case INSERT_BEFORE:
          childList.add(ix, zipper.getCurNode());
          indexCorrection.put(name, ixDiff + 1);
          break;
        case INSERT_AFTER:
          childList.add(ix + 1, zipper.getCurNode());
          indexCorrection.put(name, ixDiff + 1);
          break;
      }
      childrenMap.put(name, childList);
    }

    T newNode = nodeAdapter.withNewChildren(parent, childrenMap);
    List<Breadcrumb<T>> newBreadcrumbs = sameParent.get(0).getBreadcrumbs().subList(1, sameParent.get(0).getBreadcrumbs().size());
    return new NodeZipper<>(newNode, newBreadcrumbs, this.nodeAdapter);
  }

  private Map<T, ImmutableList<NodeZipper<T>>> zipperWithSameParent(List<NodeZipper<T>> zippers) {
    return FpKit.groupingBy(zippers, NodeZipper::getParent);
  }
}
