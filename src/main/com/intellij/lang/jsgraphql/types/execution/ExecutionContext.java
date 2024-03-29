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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.cachecontrol.CacheControl;
import com.intellij.lang.jsgraphql.types.collect.ImmutableMapWithNullValues;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.language.FragmentDefinition;
import com.intellij.lang.jsgraphql.types.language.OperationDefinition;
import com.intellij.lang.jsgraphql.types.normalized.NormalizedQueryTree;
import com.intellij.lang.jsgraphql.types.normalized.NormalizedQueryTreeFactory;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.util.FpKit;
import org.dataloader.DataLoaderRegistry;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("TypeParameterUnusedInFormals")
@PublicApi
public class ExecutionContext {

  private final GraphQLSchema graphQLSchema;
  private final ExecutionId executionId;
  private final InstrumentationState instrumentationState;
  private final ExecutionStrategy queryStrategy;
  private final ExecutionStrategy mutationStrategy;
  private final ExecutionStrategy subscriptionStrategy;
  private final ImmutableMap<String, FragmentDefinition> fragmentsByName;
  private final OperationDefinition operationDefinition;
  private final Document document;
  private final ImmutableMapWithNullValues<String, Object> variables;
  private final Object root;
  private final Object context;
  private final Object localContext;
  private final Instrumentation instrumentation;
  private final List<GraphQLError> errors = Collections.synchronizedList(new ArrayList<>());
  private final Set<ResultPath> errorPaths = new HashSet<>();
  private final DataLoaderRegistry dataLoaderRegistry;
  private final CacheControl cacheControl;
  private final Locale locale;
  private final ValueUnboxer valueUnboxer;
  private final ExecutionInput executionInput;
  private final Supplier<NormalizedQueryTree> queryTree;

  ExecutionContext(ExecutionContextBuilder builder) {
    this.graphQLSchema = builder.graphQLSchema;
    this.executionId = builder.executionId;
    this.instrumentationState = builder.instrumentationState;
    this.queryStrategy = builder.queryStrategy;
    this.mutationStrategy = builder.mutationStrategy;
    this.subscriptionStrategy = builder.subscriptionStrategy;
    this.fragmentsByName = builder.fragmentsByName;
    this.variables = ImmutableMapWithNullValues.copyOf(builder.variables);
    this.document = builder.document;
    this.operationDefinition = builder.operationDefinition;
    this.context = builder.context;
    this.root = builder.root;
    this.instrumentation = builder.instrumentation;
    this.dataLoaderRegistry = builder.dataLoaderRegistry;
    this.cacheControl = builder.cacheControl;
    this.locale = builder.locale;
    this.valueUnboxer = builder.valueUnboxer;
    this.errors.addAll(builder.errors);
    this.localContext = builder.localContext;
    this.executionInput = builder.executionInput;
    queryTree = FpKit.interThreadMemoize(
      () -> NormalizedQueryTreeFactory.createNormalizedQuery(graphQLSchema, operationDefinition, fragmentsByName, variables));
  }


  public ExecutionId getExecutionId() {
    return executionId;
  }

  public ExecutionInput getExecutionInput() {
    return executionInput;
  }

  public InstrumentationState getInstrumentationState() {
    return instrumentationState;
  }

  public Instrumentation getInstrumentation() {
    return instrumentation;
  }

  public GraphQLSchema getGraphQLSchema() {
    return graphQLSchema;
  }

  public Map<String, FragmentDefinition> getFragmentsByName() {
    return fragmentsByName;
  }

  public Document getDocument() {
    return document;
  }

  public OperationDefinition getOperationDefinition() {
    return operationDefinition;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  @SuppressWarnings("unchecked")
  public <T> T getContext() {
    return (T)context;
  }

  @SuppressWarnings("unchecked")
  public <T> T getLocalContext() {
    return (T)localContext;
  }

  @SuppressWarnings("unchecked")
  public <T> T getRoot() {
    return (T)root;
  }

  public FragmentDefinition getFragment(String name) {
    return fragmentsByName.get(name);
  }

  public DataLoaderRegistry getDataLoaderRegistry() {
    return dataLoaderRegistry;
  }

  public CacheControl getCacheControl() {
    return cacheControl;
  }

  public Locale getLocale() {
    return locale;
  }

  public ValueUnboxer getValueUnboxer() {
    return valueUnboxer;
  }

  /**
   * This method will only put one error per field path.
   *
   * @param error     the error to add
   * @param fieldPath the field path to put it under
   */
  public void addError(GraphQLError error, ResultPath fieldPath) {
    //
    // see http://facebook.github.io/graphql/#sec-Errors-and-Non-Nullability about how per
    // field errors should be handled - ie only once per field if its already there for nullability
    // but unclear if its not that error path
    //
    if (!errorPaths.add(fieldPath)) {
      return;
    }
    this.errors.add(error);
  }

  /**
   * This method will allow you to add errors into the running execution context, without a check
   * for per field unique-ness
   *
   * @param error the error to add
   */
  public void addError(GraphQLError error) {
    // see https://github.com/graphql-java/graphql-java/issues/888 on how the spec is unclear
    // on how exactly multiple errors should be handled - ie only once per field or not outside the nullability
    // aspect.
    if (error.getPath() != null) {
      this.errorPaths.add(ResultPath.fromList(error.getPath()));
    }
    this.errors.add(error);
  }

  /**
   * @return the total list of errors for this execution context
   */
  public List<GraphQLError> getErrors() {
    return ImmutableList.copyOf(errors);
  }

  public ExecutionStrategy getQueryStrategy() {
    return queryStrategy;
  }

  public ExecutionStrategy getMutationStrategy() {
    return mutationStrategy;
  }

  public ExecutionStrategy getSubscriptionStrategy() {
    return subscriptionStrategy;
  }

  public Supplier<NormalizedQueryTree> getNormalizedQueryTree() {
    return queryTree;
  }

  /**
   * This helps you transform the current ExecutionContext object into another one by starting a builder with all
   * the current values and allows you to transform it how you want.
   *
   * @param builderConsumer the consumer code that will be given a builder to transform
   * @return a new ExecutionContext object based on calling build on that builder
   */
  public ExecutionContext transform(Consumer<ExecutionContextBuilder> builderConsumer) {
    ExecutionContextBuilder builder = ExecutionContextBuilder.newExecutionContextBuilder(this);
    builderConsumer.accept(builder);
    return builder.build();
  }
}
