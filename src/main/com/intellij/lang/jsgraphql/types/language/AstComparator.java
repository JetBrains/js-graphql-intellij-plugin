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


import com.intellij.lang.jsgraphql.types.Internal;

import java.util.Iterator;
import java.util.List;

@Internal
public class AstComparator {


  public boolean isEqual(Node node1, Node node2) {
    if (null == node1) return null == node2;
    if (!node1.isEqualTo(node2)) return false;
    List<Node> childs1 = node1.getChildren();
    List<Node> childs2 = node2.getChildren();
    if (childs1.size() != childs2.size()) return false;
    for (int i = 0; i < childs1.size(); i++) {
      if (!isEqual(childs1.get(i), childs2.get(i))) return false;
    }
    return true;
  }

  public boolean isEqual(List<Node> nodes1, List<Node> nodes2) {
    if (nodes1.size() != nodes2.size()) return false;
    Iterator<Node> iter1 = nodes1.iterator();
    Iterator<Node> iter2 = nodes2.iterator();
    while (iter1.hasNext()) {
      if (!isEqual(iter1.next(), iter2.next())) return false;
    }
    return true;
  }
}
