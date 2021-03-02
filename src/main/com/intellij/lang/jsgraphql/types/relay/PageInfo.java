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
