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

/**
 * Represents pagination information in Relay about {@link com.intellij.lang.jsgraphql.types.relay.Edge edges} when used
 * inside a {@link com.intellij.lang.jsgraphql.types.relay.Connection connection}
 *
 * See <a href="https://facebook.github.io/relay/graphql/connections.htm#sec-undefined.PageInfo">https://facebook.github.io/relay/graphql/connections.htm#sec-undefined.PageInfo</a>
 */
@PublicApi
public interface PageInfo {

    /**
     * @return cursor to the first edge, or null if this page is empty.
     */
    ConnectionCursor getStartCursor();

    /**
     * @return cursor to the last edge, or null if this page is empty.
     */
    ConnectionCursor getEndCursor();

    /**
     * @return true if and only if this page is not the first page. only meaningful when you gave the {@code last} argument.
     */
    boolean isHasPreviousPage();

    /**
     * @return true if and only if this page is not the last page. only meaningful when you gave the {@code first} argument.
     */
    boolean isHasNextPage();
}
