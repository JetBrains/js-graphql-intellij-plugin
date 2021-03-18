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
package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * The result object for {@link graphql.execution.DataFetcherExceptionHandler}s
 */
@PublicApi
public class DataFetcherExceptionHandlerResult {

    private final List<GraphQLError> errors;

    private DataFetcherExceptionHandlerResult(Builder builder) {
        this.errors = builder.errors;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public static Builder newResult() {
        return new Builder();
    }

    public static Builder newResult(GraphQLError error) {
        return new Builder().error(error);
    }

    public static class Builder {

        private final List<GraphQLError> errors = new ArrayList<>();

        public Builder errors(List<GraphQLError> errors) {
            this.errors.addAll(assertNotNull(errors));
            return this;
        }

        public Builder error(GraphQLError error) {
            errors.add(assertNotNull(error));
            return this;
        }

        public DataFetcherExceptionHandlerResult build() {
            return new DataFetcherExceptionHandlerResult(this);
        }
    }
}
