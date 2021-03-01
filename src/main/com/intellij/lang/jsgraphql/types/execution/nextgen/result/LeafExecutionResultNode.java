package com.intellij.lang.jsgraphql.types.execution.nextgen.result;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.NonNullableFieldWasNullException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Internal
public class LeafExecutionResultNode extends ExecutionResultNode {

    public LeafExecutionResultNode(ExecutionStepInfo executionStepInfo,
                                   ResolvedValue resolvedValue,
                                   NonNullableFieldWasNullException nonNullableFieldWasNullException) {
        this(executionStepInfo, resolvedValue, nonNullableFieldWasNullException, Collections.emptyList());
    }

    public LeafExecutionResultNode(ExecutionStepInfo executionStepInfo,
                                   ResolvedValue resolvedValue,
                                   NonNullableFieldWasNullException nonNullableFieldWasNullException,
                                   List<GraphQLError> errors) {
        super(executionStepInfo, resolvedValue, nonNullableFieldWasNullException, Collections.emptyList(), errors);
    }


    public Object getValue() {
        return getResolvedValue().getCompletedValue();
    }

    @Override
    public ExecutionResultNode withNewChildren(List<ExecutionResultNode> children) {
        return Assert.assertShouldNeverHappen();
    }

    @Override
    public ExecutionResultNode withNewExecutionStepInfo(ExecutionStepInfo executionStepInfo) {
        return new LeafExecutionResultNode(executionStepInfo, getResolvedValue(), getNonNullableFieldWasNullException(), getErrors());
    }

    @Override
    public ExecutionResultNode withNewResolvedValue(ResolvedValue resolvedValue) {
        return new LeafExecutionResultNode(getExecutionStepInfo(), resolvedValue, getNonNullableFieldWasNullException(), getErrors());
    }

    @Override
    public ExecutionResultNode withNewErrors(List<GraphQLError> errors) {
        return new LeafExecutionResultNode(getExecutionStepInfo(), getResolvedValue(), getNonNullableFieldWasNullException(), new ArrayList<>(errors));
    }
}
