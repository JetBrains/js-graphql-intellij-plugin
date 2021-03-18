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
package com.intellij.lang.jsgraphql.types.analysis;

import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * The query depth info.
 */
@PublicApi
public class QueryDepthInfo {

    private final int depth;

    private QueryDepthInfo(int depth) {
        this.depth = depth;
    }

    /**
     * This returns the query depth.
     *
     * @return the query depth
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "QueryDepthInfo{" +
                "depth=" + depth +
                '}';
    }

    /**
     * @return a new {@link QueryDepthInfo} builder
     */
    public static Builder newQueryDepthInfo() {
        return new Builder();
    }

    @PublicApi
    public static class Builder {

        private int depth;

        private Builder() {
        }

        /**
         * The query depth.
         *
         * @param depth the depth complexity
         * @return this builder
         */
        public Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        /**
         * @return a built {@link QueryDepthInfo} object
         */
        public QueryDepthInfo build() {
            return new QueryDepthInfo(depth);
        }
    }
}
