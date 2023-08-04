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
import com.intellij.lang.jsgraphql.types.util.NodeAdapter;
import com.intellij.lang.jsgraphql.types.util.NodeLocation;

import java.util.List;
import java.util.Map;

/**
 * Adapts an Ast node to the general node from the util package
 */
@PublicApi
public class AstNodeAdapter implements NodeAdapter<Node> {

  public static final AstNodeAdapter AST_NODE_ADAPTER = new AstNodeAdapter();

  private AstNodeAdapter() {

  }

  @Override
  public Map<String, List<Node>> getNamedChildren(Node node) {
    return node.getNamedChildren().getChildren();
  }

  @Override
  public Node withNewChildren(Node node, Map<String, List<Node>> newChildren) {
    NodeChildrenContainer nodeChildrenContainer = NodeChildrenContainer.newNodeChildrenContainer(newChildren).build();
    return node.withNewChildren(nodeChildrenContainer);
  }

  @Override
  public Node removeChild(Node node, NodeLocation location) {
    return NodeUtil.removeChild(node, location);
  }
}
