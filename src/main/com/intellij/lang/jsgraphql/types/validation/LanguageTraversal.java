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
package com.intellij.lang.jsgraphql.types.validation;


import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.Node;

import java.util.ArrayList;
import java.util.List;

@Internal
public class LanguageTraversal {

  private final List<Node> path;

  public LanguageTraversal() {
    path = new ArrayList<>();
  }

  public LanguageTraversal(List<Node> basePath) {
    if (basePath != null) {
      path = basePath;
    }
    else {
      path = new ArrayList<>();
    }
  }

  public void traverse(Node root, DocumentVisitor documentVisitor) {
    traverseImpl(root, documentVisitor, path);
  }


  private void traverseImpl(Node<?> root, DocumentVisitor documentVisitor, List<Node> path) {
    documentVisitor.enter(root, path);
    path.add(root);
    List<Node> children = root.getChildren();
    for (Node child : children) {
      if (child != null) {
        traverseImpl(child, documentVisitor, path);
      }
    }
    path.remove(path.size() - 1);
    documentVisitor.leave(root, path);
  }
}
