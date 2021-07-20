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
import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.*;
import com.intellij.lang.jsgraphql.types.execution.nextgen.result.*;
import com.intellij.lang.jsgraphql.types.util.FpKit;
import com.intellij.lang.jsgraphql.types.util.NodeMultiZipper;
import com.intellij.lang.jsgraphql.types.util.NodeZipper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotEmpty;
import static com.intellij.lang.jsgraphql.types.Assert.assertTrue;
import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;
import static com.intellij.lang.jsgraphql.types.execution.nextgen.result.ResultNodeAdapter.RESULT_NODE_ADAPTER;
import static com.intellij.lang.jsgraphql.types.util.FpKit.*;
import static java.util.concurrent.CompletableFuture.completedFuture;

@Internal
public class BatchedExecutionStrategy implements ExecutionStrategy {

    ExecutionStepInfoFactory executionInfoFactory = new ExecutionStepInfoFactory();
    ValueFetcher valueFetcher = new ValueFetcher();

    FetchedValueAnalyzer fetchedValueAnalyzer = new FetchedValueAnalyzer();
    ExecutionStrategyUtil util = new ExecutionStrategyUtil();
    ExecutionHelper executionHelper = new ExecutionHelper();


    @Override
    public CompletableFuture<ExecutionResult> execute(ExecutionContext context) {
        FieldSubSelection fieldSubSelection = executionHelper.getFieldSubSelection(context);
        return executeImpl(context, fieldSubSelection)
                .thenApply(ResultNodesUtil::toExecutionResult);
    }


    public CompletableFuture<RootExecutionResultNode> executeImpl(ExecutionContext executionContext, FieldSubSelection fieldSubSelection) {
        CompletableFuture<RootExecutionResultNode> rootCF = Async.each(util.fetchSubSelection(executionContext, fieldSubSelection))
                .thenApply(RootExecutionResultNode::new);

        return rootCF.thenCompose(rootNode -> {
            NodeMultiZipper<ExecutionResultNode> unresolvedNodes = ResultNodesUtil.getUnresolvedNodes(rootNode);
            return nextStep(executionContext, unresolvedNodes);
        })
                .thenApply(multiZipper -> multiZipper.toRootNode())
                .thenApply(RootExecutionResultNode.class::cast);
    }


    private CompletableFuture<NodeMultiZipper<ExecutionResultNode>> nextStep(ExecutionContext executionContext, NodeMultiZipper<ExecutionResultNode> multizipper) {
        NodeMultiZipper<ExecutionResultNode> nextUnresolvedNodes = ResultNodesUtil.getUnresolvedNodes(multizipper.toRootNode());
        if (nextUnresolvedNodes.getZippers().size() == 0) {
            return completedFuture(nextUnresolvedNodes);
        }
        List<NodeMultiZipper<ExecutionResultNode>> groups = groupNodesIntoBatches(nextUnresolvedNodes);
        return resolveNodes(executionContext, groups).thenCompose(next -> nextStep(executionContext, next));
    }

    // all multizipper have the same root
    private CompletableFuture<NodeMultiZipper<ExecutionResultNode>> resolveNodes(ExecutionContext executionContext, List<NodeMultiZipper<ExecutionResultNode>> unresolvedNodes) {
        assertNotEmpty(unresolvedNodes, () -> "unresolvedNodes can't be empty");
        ExecutionResultNode commonRoot = unresolvedNodes.get(0).getCommonRoot();
        CompletableFuture<List<List<NodeZipper<ExecutionResultNode>>>> listListCF = Async.flatMap(unresolvedNodes,
                executionResultMultiZipper -> fetchAndAnalyze(executionContext, executionResultMultiZipper.getZippers()));

        return flatList(listListCF).thenApply(zippers -> new NodeMultiZipper<ExecutionResultNode>(commonRoot, zippers, RESULT_NODE_ADAPTER));
    }

    private List<NodeMultiZipper<ExecutionResultNode>> groupNodesIntoBatches(NodeMultiZipper<ExecutionResultNode> unresolvedZipper) {
        Map<MergedField, ImmutableList<NodeZipper<ExecutionResultNode>>> zipperBySubSelection = FpKit.groupingBy(unresolvedZipper.getZippers(),
                (executionResultZipper -> executionResultZipper.getCurNode().getMergedField()));
        return mapEntries(zipperBySubSelection, (key, value) -> new NodeMultiZipper<ExecutionResultNode>(unresolvedZipper.getCommonRoot(), value, RESULT_NODE_ADAPTER));
    }

    private CompletableFuture<List<NodeZipper<ExecutionResultNode>>> fetchAndAnalyze(ExecutionContext executionContext, List<NodeZipper<ExecutionResultNode>> unresolvedNodes) {
        assertTrue(unresolvedNodes.size() > 0, () -> "unresolvedNodes can't be empty");

        List<FieldSubSelection> fieldSubSelections = map(unresolvedNodes,
                node -> util.createFieldSubSelection(executionContext, node.getCurNode().getExecutionStepInfo(), node.getCurNode().getResolvedValue()));

        //constrain: all fieldSubSelections have the same mergedSelectionSet
        MergedSelectionSet mergedSelectionSet = fieldSubSelections.get(0).getMergedSelectionSet();

        List<CompletableFuture<List<FetchedValueAnalysis>>> fetchedValues = batchFetchForEachSubField(executionContext, fieldSubSelections, mergedSelectionSet);

        return mapBatchedResultsBack(unresolvedNodes, fetchedValues);
    }

    private CompletableFuture<List<NodeZipper<ExecutionResultNode>>> mapBatchedResultsBack(List<NodeZipper<ExecutionResultNode>> unresolvedNodes, List<CompletableFuture<List<FetchedValueAnalysis>>> fetchedValues) {
        return Async.each(fetchedValues).thenApply(fetchedValuesMatrix -> {
            List<NodeZipper<ExecutionResultNode>> result = new ArrayList<>();
            List<List<FetchedValueAnalysis>> newChildsPerNode = transposeMatrix(fetchedValuesMatrix);

            for (int i = 0; i < newChildsPerNode.size(); i++) {
                NodeZipper<ExecutionResultNode> unresolvedNodeZipper = unresolvedNodes.get(i);
                List<FetchedValueAnalysis> fetchedValuesForNode = newChildsPerNode.get(i);
                NodeZipper<ExecutionResultNode> resolvedZipper = resolveZipper(unresolvedNodeZipper, fetchedValuesForNode);
                result.add(resolvedZipper);
            }
            return result;
        });
    }

    private List<CompletableFuture<List<FetchedValueAnalysis>>> batchFetchForEachSubField(ExecutionContext executionContext,
                                                                                          List<FieldSubSelection> fieldSubSelections,
                                                                                          MergedSelectionSet mergedSelectionSet) {
        List<Object> sources = map(fieldSubSelections, FieldSubSelection::getSource);
        return mapEntries(mergedSelectionSet.getSubFields(), (name, mergedField) -> {
            List<ExecutionStepInfo> newExecutionStepInfos = newExecutionInfos(executionContext, fieldSubSelections, mergedField);
            return valueFetcher
                    .fetchBatchedValues(executionContext, sources, mergedField, newExecutionStepInfos)
                    .thenApply(fetchValue -> analyseValues(executionContext, fetchValue, newExecutionStepInfos));
        });
    }

    private List<ExecutionStepInfo> newExecutionInfos(ExecutionContext executionContext, List<FieldSubSelection> fieldSubSelections, MergedField mergedField) {
        return map(fieldSubSelections,
                subSelection -> executionInfoFactory.newExecutionStepInfoForSubField(executionContext, mergedField, subSelection.getExecutionStepInfo()));
    }

    private NodeZipper<ExecutionResultNode> resolveZipper(NodeZipper<ExecutionResultNode> unresolvedNodeZipper, List<FetchedValueAnalysis> fetchedValuesForNode) {
        UnresolvedObjectResultNode unresolvedNode = (UnresolvedObjectResultNode) unresolvedNodeZipper.getCurNode();
        List<ExecutionResultNode> newChildren = util.fetchedValueAnalysisToNodes(fetchedValuesForNode);
        ObjectExecutionResultNode newNode = unresolvedNode.withNewChildren(newChildren);
        return unresolvedNodeZipper.withNewNode(newNode);
    }


    private List<FetchedValueAnalysis> analyseValues(ExecutionContext executionContext, List<FetchedValue> fetchedValues, List<ExecutionStepInfo> executionInfos) {
        List<FetchedValueAnalysis> result = new ArrayList<>();
        for (int i = 0; i < fetchedValues.size(); i++) {
            FetchedValue fetchedValue = fetchedValues.get(i);
            ExecutionStepInfo executionStepInfo = executionInfos.get(i);
            FetchedValueAnalysis fetchedValueAnalysis = fetchedValueAnalyzer.analyzeFetchedValue(executionContext, fetchedValue, executionStepInfo);
            result.add(fetchedValueAnalysis);
        }
        return result;
    }
}
