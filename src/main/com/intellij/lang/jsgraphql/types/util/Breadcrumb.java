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

import java.util.Objects;

/**
 * A specific {@link NodeLocation} inside a node. This means  {@link #getNode()} returns a Node which has a child
 * at {@link #getLocation()}
 * <p>
 * A list of Breadcrumbs is used to identify the exact location of a specific node inside a tree.
 *
 * @param <T> the generic type of object
 */
@PublicApi
public class Breadcrumb<T> {

  private final T node;
  private final NodeLocation location;

  public Breadcrumb(T node, NodeLocation location) {
    this.node = node;
    this.location = location;
  }

  public T getNode() {
    return node;
  }

  public NodeLocation getLocation() {
    return location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Breadcrumb<?> that = (Breadcrumb<?>)o;
    return Objects.equals(node, that.node) &&
           Objects.equals(location, that.location);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(node);
    result = 31 * result + Objects.hashCode(location);
    return result;
  }
}
