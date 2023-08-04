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
package com.intellij.lang.jsgraphql.types.execution.nextgen.result;

import com.google.common.collect.ImmutableList;
import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.MergedField;
import com.intellij.lang.jsgraphql.types.execution.NonNullableFieldWasNullException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@Internal
public abstract class ExecutionResultNode {

    private final ExecutionStepInfo executionStepInfo;
    private final ResolvedValue resolvedValue;
    private final NonNullableFieldWasNullException nonNullableFieldWasNullException;
    private final ImmutableList<ExecutionResultNode> children;
    private final ImmutableList<GraphQLError> errors;

    /*
     * we are trusting here the the children list is not modified on the outside (no defensive copy)
     */
    protected ExecutionResultNode(ExecutionStepInfo executionStepInfo,
                                  ResolvedValue resolvedValue,
                                  NonNullableFieldWasNullException nonNullableFieldWasNullException,
                                  List<ExecutionResultNode> children,
                                  List<GraphQLError> errors) {
        this.resolvedValue = resolvedValue;
        this.executionStepInfo = executionStepInfo;
        this.nonNullableFieldWasNullException = nonNullableFieldWasNullException;
        this.children = ImmutableList.copyOf(assertNotNull(children));
        children.forEach(Assert::assertNotNull);
        this.errors = ImmutableList.copyOf(errors);
    }

    public List<GraphQLError> getErrors() {
        return new ArrayList<>(errors);
    }

    /*
     * can be null for the RootExecutionResultNode
     */
    public ResolvedValue getResolvedValue() {
        return resolvedValue;
    }

    public MergedField getMergedField() {
        return executionStepInfo.getField();
    }

    public ExecutionStepInfo getExecutionStepInfo() {
        return executionStepInfo;
    }

    public NonNullableFieldWasNullException getNonNullableFieldWasNullException() {
        return nonNullableFieldWasNullException;
    }

    public List<ExecutionResultNode> getChildren() {
        return this.children;
    }

    public Optional<NonNullableFieldWasNullException> getChildNonNullableException() {
        return children.stream()
                .map(ExecutionResultNode::getNonNullableFieldWasNullException)
                .filter(exception -> exception != null)
                .findFirst();
    }

    /**
     * Creates a new ExecutionResultNode of the same specific type with the new set of result children
     *
     * @param children the new children for this result node
     *
     * @return a new ExecutionResultNode with the new result children
     */
    public abstract ExecutionResultNode withNewChildren(List<ExecutionResultNode> children);

    public abstract ExecutionResultNode withNewResolvedValue(ResolvedValue resolvedValue);

    public abstract ExecutionResultNode withNewExecutionStepInfo(ExecutionStepInfo executionStepInfo);


    /**
     * Creates a new ExecutionResultNode of the same specific type with the new error collection
     *
     * @param errors the new errors for this result node
     *
     * @return a new ExecutionResultNode with the new errors
     */
    public abstract ExecutionResultNode withNewErrors(List<GraphQLError> errors);


    @Override
    public String toString() {
        return "ExecutionResultNode{" +
                "executionStepInfo=" + executionStepInfo +
                ", resolvedValue=" + resolvedValue +
                ", nonNullableFieldWasNullException=" + nonNullableFieldWasNullException +
                ", children=" + children +
                ", errors=" + errors +
                '}';
    }
}
