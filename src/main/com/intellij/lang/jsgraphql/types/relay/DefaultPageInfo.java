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


import com.intellij.lang.jsgraphql.types.PublicApi;

@PublicApi
public class DefaultPageInfo implements PageInfo {

  private final ConnectionCursor startCursor;
  private final ConnectionCursor endCursor;
  private final boolean hasPreviousPage;
  private final boolean hasNextPage;

  public DefaultPageInfo(ConnectionCursor startCursor, ConnectionCursor endCursor, boolean hasPreviousPage, boolean hasNextPage) {
    this.startCursor = startCursor;
    this.endCursor = endCursor;
    this.hasPreviousPage = hasPreviousPage;
    this.hasNextPage = hasNextPage;
  }

  @Override
  public ConnectionCursor getStartCursor() {
    return startCursor;
  }


  @Override
  public ConnectionCursor getEndCursor() {
    return endCursor;
  }

  @Override
  public boolean isHasPreviousPage() {
    return hasPreviousPage;
  }

  @Override
  public boolean isHasNextPage() {
    return hasNextPage;
  }

  @Override
  public String toString() {
    return "DefaultPageInfo{" +
           " startCursor=" + startCursor +
           ", endCursor=" + endCursor +
           ", hasPreviousPage=" + hasPreviousPage +
           ", hasNextPage=" + hasNextPage +
           '}';
  }
}
