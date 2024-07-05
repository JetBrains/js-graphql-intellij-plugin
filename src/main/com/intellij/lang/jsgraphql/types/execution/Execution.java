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


import com.intellij.lang.jsgraphql.types.*;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import com.intellij.lang.jsgraphql.types.execution.nextgen.Common;
import com.intellij.lang.jsgraphql.types.language.*;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.execution.ExecutionContextBuilder.newExecutionContextBuilder;
import static com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo.newExecutionStepInfo;
import static com.intellij.lang.jsgraphql.types.execution.ExecutionStrategyParameters.newParameters;
import static com.intellij.lang.jsgraphql.types.language.OperationDefinition.Operation.SUBSCRIPTION;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Internal
public class Execution {
  private final FieldCollector fieldCollector = new FieldCollector();
  private final ValuesResolver valuesResolver = new ValuesResolver();
  private final ExecutionStrategy queryStrategy;
  private final ExecutionStrategy mutationStrategy;
  private final ExecutionStrategy subscriptionStrategy;
  private final Instrumentation instrumentation;
  private ValueUnboxer valueUnboxer;

  public Execution(ExecutionStrategy queryStrategy,
                   ExecutionStrategy mutationStrategy,
                   ExecutionStrategy subscriptionStrategy,
                   Instrumentation instrumentation,
                   ValueUnboxer valueUnboxer) {
    this.queryStrategy = queryStrategy != null ? queryStrategy : new AsyncExecutionStrategy();
    this.mutationStrategy = mutationStrategy != null ? mutationStrategy : new AsyncSerialExecutionStrategy();
    this.subscriptionStrategy = subscriptionStrategy != null ? subscriptionStrategy : new AsyncExecutionStrategy();
    this.instrumentation = instrumentation;
    this.valueUnboxer = valueUnboxer;
  }

  public CompletableFuture<ExecutionResult> execute(Document document,
                                                    GraphQLSchema graphQLSchema,
                                                    ExecutionId executionId,
                                                    ExecutionInput executionInput,
                                                    InstrumentationState instrumentationState) {

    NodeUtil.GetOperationResult getOperationResult = NodeUtil.getOperation(document, executionInput.getOperationName());
    Map<String, FragmentDefinition> fragmentsByName = getOperationResult.fragmentsByName;
    OperationDefinition operationDefinition = getOperationResult.operationDefinition;

    Map<String, Object> inputVariables = executionInput.getVariables();
    List<VariableDefinition> variableDefinitions = operationDefinition.getVariableDefinitions();

    Map<String, Object> coercedVariables;
    try {
      coercedVariables = valuesResolver.coerceVariableValues(graphQLSchema, variableDefinitions, inputVariables);
    }
    catch (RuntimeException rte) {
      if (rte instanceof GraphQLError) {
        return completedFuture(new ExecutionResultImpl((GraphQLError)rte));
      }
      throw rte;
    }

    ExecutionContext executionContext = newExecutionContextBuilder()
      .instrumentation(instrumentation)
      .instrumentationState(instrumentationState)
      .executionId(executionId)
      .graphQLSchema(graphQLSchema)
      .queryStrategy(queryStrategy)
      .mutationStrategy(mutationStrategy)
      .subscriptionStrategy(subscriptionStrategy)
      .context(executionInput.getContext())
      .localContext(executionInput.getLocalContext())
      .root(executionInput.getRoot())
      .fragmentsByName(fragmentsByName)
      .variables(coercedVariables)
      .document(document)
      .operationDefinition(operationDefinition)
      .dataLoaderRegistry(executionInput.getDataLoaderRegistry())
      .cacheControl(executionInput.getCacheControl())
      .locale(executionInput.getLocale())
      .valueUnboxer(valueUnboxer)
      .executionInput(executionInput)
      .build();


    InstrumentationExecutionParameters parameters = new InstrumentationExecutionParameters(
      executionInput, graphQLSchema, instrumentationState
    );
    executionContext = instrumentation.instrumentExecutionContext(executionContext, parameters);
    return executeOperation(executionContext, executionInput.getRoot(), executionContext.getOperationDefinition());
  }


  private CompletableFuture<ExecutionResult> executeOperation(ExecutionContext executionContext,
                                                              Object root,
                                                              OperationDefinition operationDefinition) {

    InstrumentationExecuteOperationParameters instrumentationParams = new InstrumentationExecuteOperationParameters(executionContext);
    InstrumentationContext<ExecutionResult> executeOperationCtx = instrumentation.beginExecuteOperation(instrumentationParams);

    OperationDefinition.Operation operation = operationDefinition.getOperation();
    GraphQLObjectType operationRootType;

    try {
      operationRootType = Common.getOperationRootType(executionContext.getGraphQLSchema(), operationDefinition);
    }
    catch (RuntimeException rte) {
      if (rte instanceof GraphQLError) {
        ExecutionResult executionResult = new ExecutionResultImpl(Collections.singletonList((GraphQLError)rte));
        CompletableFuture<ExecutionResult> resultCompletableFuture = completedFuture(executionResult);

        executeOperationCtx.onDispatched(resultCompletableFuture);
        executeOperationCtx.onCompleted(executionResult, rte);
        return resultCompletableFuture;
      }
      throw rte;
    }

    FieldCollectorParameters collectorParameters = FieldCollectorParameters.newParameters()
      .schema(executionContext.getGraphQLSchema())
      .objectType(operationRootType)
      .fragments(executionContext.getFragmentsByName())
      .variables(executionContext.getVariables())
      .build();

    MergedSelectionSet fields = fieldCollector.collectFields(collectorParameters, operationDefinition.getSelectionSet());

    ResultPath path = ResultPath.rootPath();
    ExecutionStepInfo executionStepInfo = newExecutionStepInfo().type(operationRootType).path(path).build();
    NonNullableFieldValidator nonNullableFieldValidator = new NonNullableFieldValidator(executionContext, executionStepInfo);

    ExecutionStrategyParameters parameters = newParameters()
      .executionStepInfo(executionStepInfo)
      .source(root)
      .localContext(executionContext.getLocalContext())
      .fields(fields)
      .nonNullFieldValidator(nonNullableFieldValidator)
      .path(path)
      .build();

    CompletableFuture<ExecutionResult> result;
    try {
      ExecutionStrategy executionStrategy;
      if (operation == OperationDefinition.Operation.MUTATION) {
        executionStrategy = executionContext.getMutationStrategy();
      }
      else if (operation == SUBSCRIPTION) {
        executionStrategy = executionContext.getSubscriptionStrategy();
      }
      else {
        executionStrategy = executionContext.getQueryStrategy();
      }
      result = executionStrategy.execute(executionContext, parameters);
    }
    catch (NonNullableFieldWasNullException e) {
      // this means it was non null types all the way from an offending non null type
      // up to the root object type and there was a a null value some where.
      //
      // The spec says we should return null for the data in this case
      //
      // http://facebook.github.io/graphql/#sec-Errors-and-Non-Nullability
      //
      result = completedFuture(new ExecutionResultImpl(null, executionContext.getErrors()));
    }

    // note this happens NOW - not when the result completes
    executeOperationCtx.onDispatched(result);

    result = result.whenComplete(executeOperationCtx::onCompleted);

    return result;
  }
}
