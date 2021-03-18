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
package com.intellij.lang.jsgraphql.types.execution.nextgen;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.FetchedValue;
import com.intellij.lang.jsgraphql.types.execution.MergedField;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@Internal
public class FetchedValueAnalysis {

    public enum FetchedValueType {
        OBJECT,
        LIST,
        SCALAR,
        ENUM,
    }

    private final FetchedValueType valueType;
    private final List<GraphQLError> errors;
    // not applicable for LIST
    private final Object completedValue;
    private final boolean nullValue;
    // only available for LIST
    private final List<FetchedValueAnalysis> children;
    // only for object
    private final GraphQLObjectType resolvedType;
    private final ExecutionStepInfo executionStepInfo;
    // for LIST this is the whole list
    private final FetchedValue fetchedValue;

    private FetchedValueAnalysis(Builder builder) {
        this.errors = new ArrayList<>(builder.errors);
        this.errors.addAll(builder.fetchedValue.getErrors());
        this.valueType = assertNotNull(builder.valueType);
        this.completedValue = builder.completedValue;
        this.nullValue = builder.nullValue;
        this.children = builder.children;
        this.resolvedType = builder.resolvedType;
        this.executionStepInfo = assertNotNull(builder.executionInfo);
        this.fetchedValue = assertNotNull(builder.fetchedValue);
    }

    public FetchedValueType getValueType() {
        return valueType;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public Object getCompletedValue() {
        return completedValue;
    }

    public List<FetchedValueAnalysis> getChildren() {
        return children;
    }

    public boolean isNullValue() {
        return nullValue;
    }

    public FetchedValue getFetchedValue() {
        return fetchedValue;
    }

    public FetchedValueAnalysis transfrom(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder(this);
        builderConsumer.accept(builder);
        return builder.build();
    }

    public static Builder newFetchedValueAnalysis() {
        return new Builder();
    }

    public static Builder newFetchedValueAnalysis(FetchedValueType valueType) {
        return new Builder().valueType(valueType);
    }

    public static Builder newFetchedValueAnalysis(FetchedValueAnalysis existing) {
        return new Builder(existing);
    }

    public ExecutionStepInfo getExecutionStepInfo() {
        return executionStepInfo;
    }

    public GraphQLObjectType getResolvedType() {
        return resolvedType;
    }

    public MergedField getField() {
        return executionStepInfo.getField();
    }

    public String getResultKey() {
        return executionStepInfo.getResultKey();
    }

    @Override
    public String toString() {
        return "{" +
                "valueType=" + valueType +
                ", completedValue=" + completedValue +
                ", errors=" + errors +
                ", children=" + children +
                ", stepInfo=" + executionStepInfo +
                ", nullValue=" + nullValue +
                ", resolvedType=" + resolvedType +
                ", fetchedValue=" + fetchedValue +
                '}';
    }

    public static final class Builder {
        private FetchedValueType valueType;
        private final List<GraphQLError> errors = new ArrayList<>();
        private Object completedValue;
        private FetchedValue fetchedValue;
        private List<FetchedValueAnalysis> children;
        private GraphQLObjectType resolvedType;
        private boolean nullValue;
        private ExecutionStepInfo executionInfo;

        private Builder() {
        }

        private Builder(FetchedValueAnalysis existing) {
            valueType = existing.getValueType();
            errors.addAll(existing.getErrors());
            completedValue = existing.getCompletedValue();
            fetchedValue = existing.getFetchedValue();
            children = existing.getChildren();
            nullValue = existing.isNullValue();
            resolvedType = existing.getResolvedType();
            executionInfo = existing.getExecutionStepInfo();
        }


        public Builder valueType(FetchedValueType val) {
            valueType = val;
            return this;
        }

        public Builder errors(List<GraphQLError> errors) {
            this.errors.addAll(errors);
            return this;
        }

        public Builder error(GraphQLError error) {
            this.errors.add(error);
            return this;
        }


        public Builder completedValue(Object completedValue) {
            this.completedValue = completedValue;
            return this;
        }

        public Builder children(List<FetchedValueAnalysis> children) {
            this.children = children;
            return this;
        }


        public Builder nullValue() {
            this.nullValue = true;
            return this;
        }

        public Builder resolvedType(GraphQLObjectType resolvedType) {
            this.resolvedType = resolvedType;
            return this;
        }

        public Builder executionStepInfo(ExecutionStepInfo executionInfo) {
            this.executionInfo = executionInfo;
            return this;
        }

        public Builder fetchedValue(FetchedValue fetchedValue) {
            this.fetchedValue = fetchedValue;
            return this;
        }

        public FetchedValueAnalysis build() {
            return new FetchedValueAnalysis(this);
        }
    }
}
