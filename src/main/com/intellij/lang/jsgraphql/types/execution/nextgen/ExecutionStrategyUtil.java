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

    public List<CompletableFuture<ExecutionResultNode>> fetchSubSelection(ExecutionContext executionContext, FieldSubSelection fieldSubSelection) {
        List<CompletableFuture<FetchedValueAnalysis>> fetchedValueAnalysisList = fetchAndAnalyze(executionContext, fieldSubSelection);
        return fetchedValueAnalysisToNodesAsync(fetchedValueAnalysisList);
    }

    private List<CompletableFuture<FetchedValueAnalysis>> fetchAndAnalyze(ExecutionContext context, FieldSubSelection fieldSubSelection) {

        return map(fieldSubSelection.getMergedSelectionSet().getSubFieldsList(),
                mergedField -> fetchAndAnalyzeField(context, fieldSubSelection.getSource(), fieldSubSelection.getLocalContext(), mergedField, fieldSubSelection.getExecutionStepInfo()));

    }

    private CompletableFuture<FetchedValueAnalysis> fetchAndAnalyzeField(ExecutionContext context, Object source, Object localContext, MergedField mergedField,
                                                                         ExecutionStepInfo executionStepInfo) {

        ExecutionStepInfo newExecutionStepInfo = executionStepInfoFactory.newExecutionStepInfoForSubField(context, mergedField, executionStepInfo);
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

    public FieldSubSelection createFieldSubSelection(ExecutionContext executionContext, ExecutionStepInfo executionInfo, ResolvedValue resolvedValue) {
        MergedField field = executionInfo.getField();
        Object source = resolvedValue.getCompletedValue();
        Object localContext = resolvedValue.getLocalContext();

        GraphQLOutputType sourceType = executionInfo.getUnwrappedNonNullType();
        GraphQLObjectType resolvedObjectType = resolveType.resolveType(executionContext, field, source, executionInfo.getArguments(), sourceType);
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
