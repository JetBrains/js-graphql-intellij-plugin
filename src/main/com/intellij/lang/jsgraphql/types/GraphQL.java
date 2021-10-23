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
package com.intellij.lang.jsgraphql.types;

import com.intellij.lang.jsgraphql.types.execution.*;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.ChainedInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.preparsed.NoOpPreparsedDocumentProvider;
import com.intellij.lang.jsgraphql.types.execution.preparsed.PreparsedDocumentProvider;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.execution.ExecutionIdProvider.DEFAULT_EXECUTION_ID_PROVIDER;

/**
 * This class is where all graphql-java query execution begins.  It combines the objects that are needed
 * to make a successful graphql query, with the most important being the {@link com.intellij.lang.jsgraphql.types.schema.GraphQLSchema schema}
 * and the {@link com.intellij.lang.jsgraphql.types.execution.ExecutionStrategy execution strategy}
 * <p>
 * Building this object is very cheap and can be done on each execution if necessary.  Building the schema is often not
 * as cheap, especially if its parsed from graphql IDL schema format via {@link com.intellij.lang.jsgraphql.types.schema.idl.SchemaParser}.
 * <p>
 * The data for a query is returned via {@link ExecutionResult#getData()} and any errors encountered as placed in
 * {@link ExecutionResult#getErrors()}.
 *
 * <h2>Runtime Exceptions</h2>
 * <p>
 * Runtime exceptions can be thrown by the graphql engine if certain situations are encountered.  These are not errors
 * in execution but rather totally unacceptable conditions in which to execute a graphql query.
 * <ul>
 * <li>{@link com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException} - is thrown when a value cannot be serialised by a Scalar type, for example
 * a String value being coerced as an Int.
 * </li>
 *
 * <li>{@link com.intellij.lang.jsgraphql.types.execution.UnresolvedTypeException} - is thrown if a {@link com.intellij.lang.jsgraphql.types.schema.TypeResolver} fails to provide a concrete
 * object type given a interface or union type.
 * </li>
 *
 * <li>{@link com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException} - is thrown if the schema is not valid when built via
 * {@link com.intellij.lang.jsgraphql.types.schema.GraphQLSchema.Builder#build()}
 * </li>
 *
 * <li>{@link com.intellij.lang.jsgraphql.types.GraphQLException} - is thrown as a general purpose runtime exception, for example if the code cant
 * access a named field when examining a POJO.
 * </li>
 *
 * <li>{@link AssertException} - is thrown as a low level code assertion exception for truly unexpected code conditions
 * </li>
 *
 * </ul>
 */
@SuppressWarnings("Duplicates")
@PublicApi
public class GraphQL {

    private final GraphQLSchema graphQLSchema;
    private final ExecutionStrategy queryStrategy;
    private final ExecutionStrategy mutationStrategy;
    private final ExecutionStrategy subscriptionStrategy;
    private final ExecutionIdProvider idProvider;
    private final Instrumentation instrumentation;
    private final PreparsedDocumentProvider preparsedDocumentProvider;
    private final ValueUnboxer valueUnboxer;


    private GraphQL(Builder builder) {
        this.graphQLSchema = assertNotNull(builder.graphQLSchema, () -> "graphQLSchema must be non null");
        this.queryStrategy = assertNotNull(builder.queryExecutionStrategy, () -> "queryStrategy must not be null");
        this.mutationStrategy = assertNotNull(builder.mutationExecutionStrategy,
            () -> "mutationStrategy must not be null");
        this.subscriptionStrategy = assertNotNull(builder.subscriptionExecutionStrategy,
            () -> "subscriptionStrategy must not be null");
        this.idProvider = assertNotNull(builder.idProvider, () -> "idProvider must be non null");
        this.instrumentation = assertNotNull(builder.instrumentation, () -> "instrumentation must not be null");
        this.preparsedDocumentProvider = assertNotNull(builder.preparsedDocumentProvider,
            () -> "preparsedDocumentProvider must be non null");
        this.valueUnboxer = assertNotNull(builder.valueUnboxer, () -> "valueUnboxer must not be null");
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
        Builder builder = new Builder(this.graphQLSchema);
        builder
            .queryExecutionStrategy(this.queryStrategy)
            .mutationExecutionStrategy(this.mutationStrategy)
            .subscriptionExecutionStrategy(this.subscriptionStrategy)
            .executionIdProvider(Optional.ofNullable(this.idProvider).orElse(builder.idProvider))
            .instrumentation(Optional.ofNullable(this.instrumentation).orElse(builder.instrumentation))
            .preparsedDocumentProvider(
                Optional.ofNullable(this.preparsedDocumentProvider).orElse(builder.preparsedDocumentProvider));

        builderConsumer.accept(builder);

        return builder.build();
    }

    @PublicApi
    public static class Builder {
        private GraphQLSchema graphQLSchema;
        private ExecutionStrategy queryExecutionStrategy;
        private ExecutionStrategy mutationExecutionStrategy;
        private ExecutionStrategy subscriptionExecutionStrategy;
        private DataFetcherExceptionHandler defaultExceptionHandler = new SimpleDataFetcherExceptionHandler();
        private ExecutionIdProvider idProvider = DEFAULT_EXECUTION_ID_PROVIDER;
        private Instrumentation instrumentation = null; // deliberate default here
        private PreparsedDocumentProvider preparsedDocumentProvider = NoOpPreparsedDocumentProvider.INSTANCE;
        private boolean doNotAddDefaultInstrumentations = false;
        private ValueUnboxer valueUnboxer = ValueUnboxer.DEFAULT;


        public Builder(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = graphQLSchema;
        }

        public Builder schema(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = assertNotNull(graphQLSchema, () -> "GraphQLSchema must be non null");
            return this;
        }

        public Builder queryExecutionStrategy(ExecutionStrategy executionStrategy) {
            this.queryExecutionStrategy = assertNotNull(executionStrategy,
                () -> "Query ExecutionStrategy must be non null");
            return this;
        }

        public Builder mutationExecutionStrategy(ExecutionStrategy executionStrategy) {
            this.mutationExecutionStrategy = assertNotNull(executionStrategy,
                () -> "Mutation ExecutionStrategy must be non null");
            return this;
        }

        public Builder subscriptionExecutionStrategy(ExecutionStrategy executionStrategy) {
            this.subscriptionExecutionStrategy = assertNotNull(executionStrategy,
                () -> "Subscription ExecutionStrategy must be non null");
            return this;
        }

        /**
         * This allows you to set a default {@link com.intellij.lang.jsgraphql.types.execution.DataFetcherExceptionHandler} that will be used to handle exceptions that happen
         * in {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} invocations.
         *
         * @param dataFetcherExceptionHandler the default handler for data fetching exception
         * @return this builder
         */
        public Builder defaultDataFetcherExceptionHandler(DataFetcherExceptionHandler dataFetcherExceptionHandler) {
            this.defaultExceptionHandler = assertNotNull(dataFetcherExceptionHandler,
                () -> "The DataFetcherExceptionHandler must be non null");
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

        /**
         * For performance reasons you can opt into situation where the default instrumentations (such
         * as {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation} will not be
         * automatically added into the graphql instance.
         * <p>
         * For most situations this is not needed unless you are really pushing the boundaries of performance
         * <p>
         * By default a certain graphql instrumentations will be added to the mix to more easily enable certain functionality.  This
         * allows you to stop this behavior
         *
         * @return this builder
         */
        public Builder doNotAddDefaultInstrumentations() {
            this.doNotAddDefaultInstrumentations = true;
            return this;
        }

        public Builder valueUnboxer(ValueUnboxer valueUnboxer) {
            this.valueUnboxer = valueUnboxer;
            return this;
        }

        public GraphQL build() {
            // we use the data fetcher exception handler unless they set their own strategy in which case bets are off
            if (queryExecutionStrategy == null) {
                this.queryExecutionStrategy = new AsyncExecutionStrategy(this.defaultExceptionHandler);
            }
            if (mutationExecutionStrategy == null) {
                this.mutationExecutionStrategy = new AsyncSerialExecutionStrategy(this.defaultExceptionHandler);
            }
            if (subscriptionExecutionStrategy == null) {
                this.subscriptionExecutionStrategy = new SubscriptionExecutionStrategy(this.defaultExceptionHandler);
            }

            this.instrumentation = checkInstrumentationDefaultState(this.instrumentation,
                this.doNotAddDefaultInstrumentations);
            return new GraphQL(this);
        }
    }

    private static Instrumentation checkInstrumentationDefaultState(Instrumentation instrumentation,
                                                                    boolean doNotAddDefaultInstrumentations) {
        if (doNotAddDefaultInstrumentations) {
            return instrumentation == null ? SimpleInstrumentation.INSTANCE : instrumentation;
        }
        if (instrumentation instanceof DataLoaderDispatcherInstrumentation) {
            return instrumentation;
        }
        if (instrumentation == null) {
            return new DataLoaderDispatcherInstrumentation();
        }

        //
        // if we don't have a DataLoaderDispatcherInstrumentation in play, we add one.  We want DataLoader to be 1st class in graphql without requiring
        // people to remember to wire it in.  Later we may decide to have more default instrumentations but for now its just the one
        //
        List<Instrumentation> instrumentationList = new ArrayList<>();
        if (instrumentation instanceof ChainedInstrumentation) {
            instrumentationList.addAll(((ChainedInstrumentation) instrumentation).getInstrumentations());
        } else {
            instrumentationList.add(instrumentation);
        }
        boolean containsDLInstrumentation = instrumentationList.stream().anyMatch(
            instr -> instr instanceof DataLoaderDispatcherInstrumentation);
        if (!containsDLInstrumentation) {
            instrumentationList.add(new DataLoaderDispatcherInstrumentation());
        }
        return new ChainedInstrumentation(instrumentationList);
    }
}
