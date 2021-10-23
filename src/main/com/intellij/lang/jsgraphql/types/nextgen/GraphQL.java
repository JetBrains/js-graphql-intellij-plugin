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
package com.intellij.lang.jsgraphql.types.nextgen;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ExecutionIdProvider;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.nextgen.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.nextgen.DefaultExecutionStrategy;
import com.intellij.lang.jsgraphql.types.execution.nextgen.ExecutionStrategy;
import com.intellij.lang.jsgraphql.types.execution.preparsed.NoOpPreparsedDocumentProvider;
import com.intellij.lang.jsgraphql.types.execution.preparsed.PreparsedDocumentProvider;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@SuppressWarnings("Duplicates")
@Internal
public class GraphQL {
    private final GraphQLSchema graphQLSchema;
    private final ExecutionStrategy executionStrategy;
    private final ExecutionIdProvider idProvider;
    private final Instrumentation instrumentation;
    private final PreparsedDocumentProvider preparsedDocumentProvider;

    public GraphQL(Builder builder) {
        this.graphQLSchema = builder.graphQLSchema;
        this.executionStrategy = builder.executionStrategy;
        this.idProvider = builder.idProvider;
        this.preparsedDocumentProvider = builder.preparsedDocumentProvider;
        this.instrumentation = builder.instrumentation;
    }

    /**
     * Helps you build a GraphQL object ready to execute queries
     *
     * @param graphQLSchema the schema to use
     * @return a builder of GraphQL objects
     */
    public static Builder newGraphQL(GraphQLSchema graphQLSchema) {
        return new Builder(graphQLSchema);
    }

    /**
     * This helps you transform the current GraphQL object into another one by starting a builder with all
     * the current values and allows you to transform it how you want.
     *
     * @param builderConsumer the consumer code that will be given a builder to transform
     * @return a new GraphQL object based on calling build on that builder
     */
    public GraphQL transform(Consumer<Builder> builderConsumer) {
        Builder builder = new Builder(this);
        builderConsumer.accept(builder);
        return builder.build();
    }


    public static class Builder {
        private GraphQLSchema graphQLSchema;
        private ExecutionStrategy executionStrategy = new DefaultExecutionStrategy();
        private ExecutionIdProvider idProvider = ExecutionIdProvider.DEFAULT_EXECUTION_ID_PROVIDER;
        private Instrumentation instrumentation = new Instrumentation() {
        };
        private PreparsedDocumentProvider preparsedDocumentProvider = NoOpPreparsedDocumentProvider.INSTANCE;


        public Builder(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = graphQLSchema;
        }

        public Builder(GraphQL graphQL) {
            this.graphQLSchema = graphQL.graphQLSchema;
            this.executionStrategy = graphQL.executionStrategy;
            this.idProvider = graphQL.idProvider;
            this.instrumentation = graphQL.instrumentation;
        }

        public Builder schema(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = assertNotNull(graphQLSchema, () -> "GraphQLSchema must be non null");
            return this;
        }

        public Builder executionStrategy(ExecutionStrategy executionStrategy) {
            this.executionStrategy = assertNotNull(executionStrategy, () -> "ExecutionStrategy must be non null");
            return this;
        }

        public Builder instrumentation(Instrumentation instrumentation) {
            this.instrumentation = assertNotNull(instrumentation, () -> "Instrumentation must be non null");
            return this;
        }

        public Builder preparsedDocumentProvider(PreparsedDocumentProvider preparsedDocumentProvider) {
            this.preparsedDocumentProvider = assertNotNull(preparsedDocumentProvider,
                () -> "PreparsedDocumentProvider must be non null");
            return this;
        }

        public Builder executionIdProvider(ExecutionIdProvider executionIdProvider) {
            this.idProvider = assertNotNull(executionIdProvider, () -> "ExecutionIdProvider must be non null");
            return this;
        }

        public GraphQL build() {
            assertNotNull(graphQLSchema, () -> "graphQLSchema must be non null");
            return new GraphQL(this);
        }
    }
}
