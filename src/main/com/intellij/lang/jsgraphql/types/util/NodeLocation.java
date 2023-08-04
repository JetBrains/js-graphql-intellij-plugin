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
 * General position of a Node inside a parent.
 * <p>
 * Can be an index or a name with an index.
 */
@PublicApi
public class NodeLocation {

  private final String name;
  private final int index;

  public NodeLocation(String name, int index) {
    this.name = name;
    this.index = index;
  }

  /**
   * @return the name or null if there is no name
   */
  public String getName() {
    return name;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public String toString() {
    return "NodeLocation{" +
           "name='" + name + '\'' +
           ", index=" + index +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NodeLocation that = (NodeLocation)o;
    return index == that.index &&
           Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + Objects.hashCode(name);
    result = 31 * result + Integer.hashCode(index);
    return result;
  }
}
