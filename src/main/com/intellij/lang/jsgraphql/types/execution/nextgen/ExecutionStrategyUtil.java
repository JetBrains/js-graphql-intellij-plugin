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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.*;
import com.intellij.lang.jsgraphql.types.execution.nextgen.result.ExecutionResultNode;
import com.intellij.lang.jsgraphql.types.execution.nextgen.result.ResolvedValue;
import com.intellij.lang.jsgraphql.types.schema.GraphQLObjectType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLOutputType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;
import static com.intellij.lang.jsgraphql.types.execution.FieldCollectorParameters.newParameters;

@Internal
public class ExecutionStrategyUtil {

  ExecutionStepInfoFactory executionStepInfoFactory = new ExecutionStepInfoFactory();
  FetchedValueAnalyzer fetchedValueAnalyzer = new FetchedValueAnalyzer();
  ValueFetcher valueFetcher = new ValueFetcher();
  ResultNodesCreator resultNodesCreator = new ResultNodesCreator();
  ResolveType resolveType = new ResolveType();
  FieldCollector fieldCollector = new FieldCollector();

  public List<CompletableFuture<ExecutionResultNode>> fetchSubSelection(ExecutionContext executionContext,
                                                                        FieldSubSelection fieldSubSelection) {
    List<CompletableFuture<FetchedValueAnalysis>> fetchedValueAnalysisList = fetchAndAnalyze(executionContext, fieldSubSelection);
    return fetchedValueAnalysisToNodesAsync(fetchedValueAnalysisList);
  }

  private List<CompletableFuture<FetchedValueAnalysis>> fetchAndAnalyze(ExecutionContext context, FieldSubSelection fieldSubSelection) {

    return map(fieldSubSelection.getMergedSelectionSet().getSubFieldsList(),
               mergedField -> fetchAndAnalyzeField(context, fieldSubSelection.getSource(), fieldSubSelection.getLocalContext(), mergedField,
                                                   fieldSubSelection.getExecutionStepInfo()));
  }

  private CompletableFuture<FetchedValueAnalysis> fetchAndAnalyzeField(ExecutionContext context,
                                                                       Object source,
                                                                       Object localContext,
                                                                       MergedField mergedField,
                                                                       ExecutionStepInfo executionStepInfo) {

    ExecutionStepInfo newExecutionStepInfo =
      executionStepInfoFactory.newExecutionStepInfoForSubField(context, mergedField, executionStepInfo);
    return valueFetcher
      .fetchValue(context, source, localContext, mergedField, newExecutionStepInfo)
      .thenApply(fetchValue -> analyseValue(context, fetchValue, newExecutionStepInfo));
  }

  private List<CompletableFuture<ExecutionResultNode>> fetchedValueAnalysisToNodesAsync(List<CompletableFuture<FetchedValueAnalysis>> list) {
    return Async.map(list, fetchedValueAnalysis -> resultNodesCreator.createResultNode(fetchedValueAnalysis));
  }

  public List<ExecutionResultNode> fetchedValueAnalysisToNodes(List<FetchedValueAnalysis> fetchedValueAnalysisList) {
    return map(fetchedValueAnalysisList, fetchedValueAnalysis -> resultNodesCreator.createResultNode(fetchedValueAnalysis));
  }


  private FetchedValueAnalysis analyseValue(ExecutionContext executionContext, FetchedValue fetchedValue, ExecutionStepInfo executionInfo) {
    FetchedValueAnalysis fetchedValueAnalysis = fetchedValueAnalyzer.analyzeFetchedValue(executionContext, fetchedValue, executionInfo);
    return fetchedValueAnalysis;
  }

  public FieldSubSelection createFieldSubSelection(ExecutionContext executionContext,
                                                   ExecutionStepInfo executionInfo,
                                                   ResolvedValue resolvedValue) {
    MergedField field = executionInfo.getField();
    Object source = resolvedValue.getCompletedValue();
    Object localContext = resolvedValue.getLocalContext();

    GraphQLOutputType sourceType = executionInfo.getUnwrappedNonNullType();
    GraphQLObjectType resolvedObjectType =
      resolveType.resolveType(executionContext, field, source, executionInfo.getArguments(), sourceType);
    FieldCollectorParameters collectorParameters = newParameters()
      .schema(executionContext.getGraphQLSchema())
      .objectType(resolvedObjectType)
      .fragments(executionContext.getFragmentsByName())
      .variables(executionContext.getVariables())
      .build();
    MergedSelectionSet subFields = fieldCollector.collectFields(collectorParameters,
                                                                executionInfo.getField());

    // it is not really a new step but rather a refinement
    ExecutionStepInfo newExecutionStepInfoWithResolvedType = executionInfo.changeTypeWithPreservedNonNull(resolvedObjectType);

    return FieldSubSelection.newFieldSubSelection()
      .source(source)
      .localContext(localContext)
      .mergedSelectionSet(subFields)
      .executionInfo(newExecutionStepInfoWithResolvedType)
      .build();
  }
}
