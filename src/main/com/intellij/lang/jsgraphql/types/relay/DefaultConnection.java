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
package com.intellij.lang.jsgraphql.types.relay;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.Collections;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * A default implementation of {@link com.intellij.lang.jsgraphql.types.relay.Connection}
 */
@PublicApi
public class DefaultConnection<T> implements Connection<T> {

  private final ImmutableList<Edge<T>> edges;
  private final PageInfo pageInfo;

  /**
   * A connection consists of a list of edges and page info
   *
   * @param edges    a non null list of edges
   * @param pageInfo a non null page info
   * @throws IllegalArgumentException if edges or page info is null. use {@link Collections#emptyList()} for empty edges.
   */
  public DefaultConnection(List<Edge<T>> edges, PageInfo pageInfo) {
    this.edges = ImmutableList.copyOf(assertNotNull(edges, () -> "edges cannot be null"));
    this.pageInfo = assertNotNull(pageInfo, () -> "page info cannot be null");
  }

  @Override
  public List<Edge<T>> getEdges() {
    return edges;
  }

  @Override
  public PageInfo getPageInfo() {
    return pageInfo;
  }

  @Override
  public String toString() {
    return "DefaultConnection{" +
           "edges=" + edges +
           ", pageInfo=" + pageInfo +
           '}';
  }
}
