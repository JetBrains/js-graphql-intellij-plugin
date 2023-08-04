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

import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.nextgen.result.*;
import com.intellij.lang.jsgraphql.types.util.NodeMultiZipper;
import com.intellij.lang.jsgraphql.types.util.NodeZipper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.intellij.lang.jsgraphql.types.collect.ImmutableKit.map;
import static com.intellij.lang.jsgraphql.types.execution.Async.each;
import static com.intellij.lang.jsgraphql.types.execution.Async.mapCompose;

@Internal
public class DefaultExecutionStrategy implements ExecutionStrategy {

  ExecutionStrategyUtil util = new ExecutionStrategyUtil();
  ExecutionHelper executionHelper = new ExecutionHelper();

  @Override
  public CompletableFuture<ExecutionResult> execute(ExecutionContext context) {
    FieldSubSelection fieldSubSelection = executionHelper.getFieldSubSelection(context);
    return executeImpl(context, fieldSubSelection)
      .thenApply(ResultNodesUtil::toExecutionResult);
  }

  /*
   * the fundamental algorithm is:
   * - fetch sub selection and analyze it
   * - convert the fetched value analysis into result node
   * - get all unresolved result nodes and resolve the sub selection (start again recursively)
   */
  public CompletableFuture<RootExecutionResultNode> executeImpl(ExecutionContext context, FieldSubSelection fieldSubSelection) {
    return resolveSubSelection(context, fieldSubSelection)
      .thenApply(RootExecutionResultNode::new);
  }

  private CompletableFuture<List<ExecutionResultNode>> resolveSubSelection(ExecutionContext executionContext,
                                                                           FieldSubSelection fieldSubSelection) {
    List<CompletableFuture<ExecutionResultNode>> namedNodesCFList =
      mapCompose(util.fetchSubSelection(executionContext, fieldSubSelection), node -> resolveAllChildNodes(executionContext, node));
    return each(namedNodesCFList);
  }

  private CompletableFuture<ExecutionResultNode> resolveAllChildNodes(ExecutionContext context, ExecutionResultNode node) {
    NodeMultiZipper<ExecutionResultNode> unresolvedNodes = ResultNodesUtil.getUnresolvedNodes(node);
    List<CompletableFuture<NodeZipper<ExecutionResultNode>>> resolvedNodes =
      map(unresolvedNodes.getZippers(), unresolvedNode -> resolveNode(context, unresolvedNode));
    return resolvedNodesToResultNode(unresolvedNodes, resolvedNodes);
  }

  private CompletableFuture<NodeZipper<ExecutionResultNode>> resolveNode(ExecutionContext executionContext,
                                                                         NodeZipper<ExecutionResultNode> unresolvedNode) {
    ExecutionStepInfo executionStepInfo = unresolvedNode.getCurNode().getExecutionStepInfo();
    ResolvedValue resolvedValue = unresolvedNode.getCurNode().getResolvedValue();
    FieldSubSelection fieldSubSelection = util.createFieldSubSelection(executionContext, executionStepInfo, resolvedValue);
    return resolveSubSelection(executionContext, fieldSubSelection)
      .thenApply(
        resolvedChildMap -> unresolvedNode.withNewNode(new ObjectExecutionResultNode(executionStepInfo, resolvedValue, resolvedChildMap)));
  }

  private CompletableFuture<ExecutionResultNode> resolvedNodesToResultNode(
    NodeMultiZipper<ExecutionResultNode> unresolvedNodes,
    List<CompletableFuture<NodeZipper<ExecutionResultNode>>> resolvedNodes) {
    return each(resolvedNodes)
      .thenApply(unresolvedNodes::withReplacedZippers)
      .thenApply(NodeMultiZipper::toRootNode);
  }
}
