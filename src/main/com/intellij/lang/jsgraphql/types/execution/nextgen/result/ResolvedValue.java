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
package com.intellij.lang.jsgraphql.types.execution.nextgen.result;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@Internal
public class ResolvedValue {

    private final Object completedValue;
    private final Object localContext;
    private final boolean nullValue;
    private final ImmutableList<GraphQLError> errors;

    private ResolvedValue(Builder builder) {
        this.completedValue = builder.completedValue;
        this.localContext = builder.localContext;
        this.nullValue = builder.nullValue;
        this.errors = ImmutableList.copyOf(builder.errors);
    }

    public Object getCompletedValue() {
        return completedValue;
    }

    public Object getLocalContext() {
        return localContext;
    }

    public boolean isNullValue() {
        return nullValue;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public static Builder newResolvedValue() {
        return new Builder();
    }


    public ResolvedValue transform(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder(this);
        builderConsumer.accept(builder);
        return builder.build();
    }


    public static class Builder {
        private Object completedValue;
        private Object localContext;
        private boolean nullValue;
        private List<GraphQLError> errors = Collections.emptyList();

        private Builder() {

        }

        private Builder(ResolvedValue existing) {
            this.completedValue = existing.completedValue;
            this.localContext = existing.localContext;
            this.nullValue = existing.nullValue;
            this.errors = existing.errors;
        }

        public Builder completedValue(Object completedValue) {
            this.completedValue = completedValue;
            return this;
        }

        public Builder localContext(Object localContext) {
            this.localContext = localContext;
            return this;
        }

        public Builder nullValue(boolean nullValue) {
            this.nullValue = nullValue;
            return this;
        }

        public Builder errors(List<GraphQLError> errors) {
            this.errors = new ArrayList<>(errors);
            return this;
        }

        public ResolvedValue build() {
            return new ResolvedValue(this);
        }
    }

}
