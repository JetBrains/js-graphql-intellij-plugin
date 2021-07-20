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
 * The query complexity info.
 */
@PublicApi
public class QueryComplexityInfo {

    private final int complexity;

    private QueryComplexityInfo(int complexity) {
        this.complexity = complexity;
    }

    /**
     * This returns the query complexity.
     *
     * @return the query complexity
     */
    public int getComplexity() {
        return complexity;
    }

    @Override
    public String toString() {
        return "QueryComplexityInfo{" +
                "complexity=" + complexity +
                '}';
    }

    /**
     * @return a new {@link QueryComplexityInfo} builder
     */
    public static Builder newQueryComplexityInfo() {
        return new Builder();
    }

    @PublicApi
    public static class Builder {

        private int complexity;

        private Builder() {
        }

        /**
         * The query complexity.
         *
         * @param complexity the query complexity
         * @return this builder
         */
        public Builder complexity(int complexity) {
            this.complexity = complexity;
            return this;
        }

        /**
         * @return a built {@link QueryComplexityInfo} object
         */
        public QueryComplexityInfo build() {
            return new QueryComplexityInfo(complexity);
        }
    }
}
