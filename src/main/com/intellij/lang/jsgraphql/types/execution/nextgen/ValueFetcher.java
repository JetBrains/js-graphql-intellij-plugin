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


import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.ExceptionWhileDataFetching;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.collect.ImmutableKit;
import com.intellij.lang.jsgraphql.types.execution.*;
import com.intellij.lang.jsgraphql.types.execution.directives.QueryDirectivesImpl;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.normalized.NormalizedField;
import com.intellij.lang.jsgraphql.types.normalized.NormalizedQueryTree;
import com.intellij.lang.jsgraphql.types.schema.*;
import com.intellij.lang.jsgraphql.types.util.FpKit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironmentImpl.newDataFetchingEnvironment;
import static java.util.Collections.singletonList;

@Internal
public class ValueFetcher {


  ValuesResolver valuesResolver = new ValuesResolver();

  public static final Object NULL_VALUE = new Object();

  public ValueFetcher() {
  }


  public CompletableFuture<List<FetchedValue>> fetchBatchedValues(ExecutionContext executionContext,
                                                                  List<Object> sources,
                                                                  MergedField field,
                                                                  List<ExecutionStepInfo> executionInfos) {
    ExecutionStepInfo executionStepInfo = executionInfos.get(0);
    // TODO - add support for field context to batching code
    Object todoLocalContext = null;
    if (isDataFetcherBatched(executionContext, executionStepInfo)) {
      return fetchValue(executionContext, sources, todoLocalContext, field, executionStepInfo)
        .thenApply(fetchedValue -> extractBatchedValues(fetchedValue, sources.size()));
    }
    else {
      List<CompletableFuture<FetchedValue>> fetchedValues = new ArrayList<>();
      for (int i = 0; i < sources.size(); i++) {
        fetchedValues.add(fetchValue(executionContext, sources.get(i), todoLocalContext, field, executionInfos.get(i)));
      }
      return Async.each(fetchedValues);
    }
  }

  @SuppressWarnings("unchecked")
  private List<FetchedValue> extractBatchedValues(FetchedValue fetchedValueContainingList, int expectedSize) {
    List<Object> list = (List<Object>)fetchedValueContainingList.getFetchedValue();
    Assert.assertTrue(list.size() == expectedSize, () -> "Unexpected result size");
    List<FetchedValue> result = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      List<GraphQLError> errors;
      if (i == 0) {
        errors = fetchedValueContainingList.getErrors();
      }
      else {
        errors = Collections.emptyList();
      }
      FetchedValue fetchedValue = FetchedValue.newFetchedValue()
        .fetchedValue(list.get(i))
        .rawFetchedValue(fetchedValueContainingList.getRawFetchedValue())
        .errors(errors)
        .localContext(fetchedValueContainingList.getLocalContext())
        .build();
      result.add(fetchedValue);
    }
    return result;
  }

  private GraphQLFieldsContainer getFieldsContainer(ExecutionStepInfo executionStepInfo) {
    GraphQLOutputType type = executionStepInfo.getParent().getType();
    return (GraphQLFieldsContainer)GraphQLTypeUtil.unwrapAll(type);
  }

  private boolean isDataFetcherBatched(ExecutionContext executionContext, ExecutionStepInfo executionStepInfo) {
    GraphQLFieldsContainer parentType = getFieldsContainer(executionStepInfo);
    GraphQLFieldDefinition fieldDef = executionStepInfo.getFieldDefinition();
    DataFetcher dataFetcher = executionContext.getGraphQLSchema().getCodeRegistry().getDataFetcher(parentType, fieldDef);
    return dataFetcher instanceof BatchedDataFetcher;
  }

  public CompletableFuture<FetchedValue> fetchValue(ExecutionContext executionContext,
                                                    Object source,
                                                    Object localContext,
                                                    MergedField sameFields,
                                                    ExecutionStepInfo executionInfo) {
    Field field = sameFields.getSingleField();
    GraphQLFieldDefinition fieldDef = executionInfo.getFieldDefinition();

    GraphQLCodeRegistry codeRegistry = executionContext.getGraphQLSchema().getCodeRegistry();
    GraphQLFieldsContainer parentType = getFieldsContainer(executionInfo);

    Supplier<Map<String, Object>> argumentValues = FpKit.intraThreadMemoize(
      () -> valuesResolver.getArgumentValues(codeRegistry, fieldDef.getArguments(), field.getArguments(), executionContext.getVariables()));

    QueryDirectivesImpl queryDirectives =
      new QueryDirectivesImpl(sameFields, executionContext.getGraphQLSchema(), executionContext.getVariables());

    GraphQLOutputType fieldType = fieldDef.getType();

    Supplier<NormalizedQueryTree> normalizedQuery = executionContext.getNormalizedQueryTree();
    Supplier<NormalizedField> normalisedField =
      () -> normalizedQuery.get().getNormalizedField(sameFields, executionInfo.getObjectType(), executionInfo.getPath());
    DataFetchingFieldSelectionSet selectionSet = DataFetchingFieldSelectionSetImpl.newCollector(fieldType, normalisedField);

    DataFetchingEnvironment environment = newDataFetchingEnvironment(executionContext)
      .source(source)
      .localContext(localContext)
      .arguments(argumentValues)
      .fieldDefinition(fieldDef)
      .mergedField(sameFields)
      .fieldType(fieldType)
      .executionStepInfo(executionInfo)
      .parentType(parentType)
      .selectionSet(selectionSet)
      .queryDirectives(queryDirectives)
      .build();

    ExecutionId executionId = executionContext.getExecutionId();
    ResultPath path = executionInfo.getPath();
    return callDataFetcher(codeRegistry, parentType, fieldDef, environment, executionId, path)
      .thenApply(rawFetchedValue -> FetchedValue.newFetchedValue()
        .fetchedValue(rawFetchedValue)
        .rawFetchedValue(rawFetchedValue)
        .build())
      .exceptionally(exception -> handleExceptionWhileFetching(field, path, exception))
      .thenApply(result -> unboxPossibleDataFetcherResult(sameFields, path, result, localContext))
      .thenApply(this::unboxPossibleOptional);
  }

  private FetchedValue handleExceptionWhileFetching(Field field, ResultPath path, Throwable exception) {
    ExceptionWhileDataFetching exceptionWhileDataFetching = new ExceptionWhileDataFetching(path, exception, field.getSourceLocation());
    return FetchedValue.newFetchedValue().errors(singletonList(exceptionWhileDataFetching)).build();
  }

  private FetchedValue unboxPossibleOptional(FetchedValue result) {
    return result.transform(
      builder -> builder.fetchedValue(DefaultValueUnboxer.unboxValue(result.getFetchedValue()))
    );
  }

  private CompletableFuture<Object> callDataFetcher(GraphQLCodeRegistry codeRegistry,
                                                    GraphQLFieldsContainer parentType,
                                                    GraphQLFieldDefinition fieldDef,
                                                    DataFetchingEnvironment environment,
                                                    ExecutionId executionId,
                                                    ResultPath path) {
    CompletableFuture<Object> result = new CompletableFuture<>();
    try {
      DataFetcher dataFetcher = codeRegistry.getDataFetcher(parentType, fieldDef);
      Object fetchedValueRaw = dataFetcher.get(environment);
      handleFetchedValue(fetchedValueRaw, result);
    }
    catch (Exception e) {
      result.completeExceptionally(e);
    }
    return result;
  }

  private void handleFetchedValue(Object fetchedValue, CompletableFuture<Object> cf) {
    if (fetchedValue == null) {
      cf.complete(NULL_VALUE);
      return;
    }
    if (fetchedValue instanceof CompletionStage) {
      //noinspection unchecked
      CompletionStage<Object> stage = (CompletionStage<Object>)fetchedValue;
      stage.whenComplete((value, throwable) -> {
        if (throwable != null) {
          cf.completeExceptionally(throwable);
        }
        else {
          cf.complete(value);
        }
      });
      return;
    }
    cf.complete(fetchedValue);
  }

  private FetchedValue unboxPossibleDataFetcherResult(MergedField sameField,
                                                      ResultPath resultPath,
                                                      FetchedValue result,
                                                      Object localContext) {
    if (result.getFetchedValue() instanceof DataFetcherResult) {

      DataFetcherResult<?> dataFetcherResult = (DataFetcherResult)result.getFetchedValue();
      List<GraphQLError> addErrors = ImmutableList.copyOf(dataFetcherResult.getErrors());
      List<GraphQLError> newErrors = ImmutableKit.concatLists(result.getErrors(), addErrors);

      Object newLocalContext = dataFetcherResult.getLocalContext();
      if (newLocalContext == null) {
        // if the field returns nothing then they get the context of their parent field
        newLocalContext = localContext;
      }
      return FetchedValue.newFetchedValue()
        .fetchedValue(dataFetcherResult.getData())
        .rawFetchedValue(result.getRawFetchedValue())
        .errors(newErrors)
        .localContext(newLocalContext)
        .build();
    }
    else {
      return result;
    }
  }
}
