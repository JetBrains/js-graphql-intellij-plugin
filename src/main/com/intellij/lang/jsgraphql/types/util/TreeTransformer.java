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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@PublicApi
public class TreeTransformer<T> {

  private final NodeAdapter<T> nodeAdapter;

  public TreeTransformer(NodeAdapter<T> nodeAdapter) {
    this.nodeAdapter = nodeAdapter;
  }

  public T transform(T root, TraverserVisitor<T> traverserVisitor) {
    return transform(root, traverserVisitor, Collections.emptyMap());
  }

  public T transform(T root, TraverserVisitor<T> traverserVisitor, Map<Class<?>, Object> rootVars) {
    assertNotNull(root);


    TraverserVisitor<T> nodeTraverserVisitor = new TraverserVisitor<T>() {

      @Override
      public TraversalControl enter(TraverserContext<T> context) {
        NodeZipper<T> nodeZipper = new NodeZipper<>(context.thisNode(), context.getBreadcrumbs(), nodeAdapter);
        context.setVar(NodeZipper.class, nodeZipper);
        context.setVar(NodeAdapter.class, nodeAdapter);
        return traverserVisitor.enter(context);
      }

      @Override
      public TraversalControl leave(TraverserContext<T> context) {
        return traverserVisitor.leave(context);
      }

      @Override
      public TraversalControl backRef(TraverserContext<T> context) {
        return traverserVisitor.backRef(context);
      }
    };

    List<NodeZipper<T>> zippers = new LinkedList<>();
    Traverser<T> traverser = Traverser.depthFirstWithNamedChildren(nodeAdapter::getNamedChildren, zippers, null);
    traverser.rootVars(rootVars);
    traverser.traverse(root, nodeTraverserVisitor);

    NodeMultiZipper<T> multiZipper = NodeMultiZipper.newNodeMultiZipperTrusted(root, zippers, nodeAdapter);
    return multiZipper.toRootNode();
  }
}
