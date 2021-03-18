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

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertShouldNeverHappen;

@Internal
public class RootExecutionResultNode extends ObjectExecutionResultNode {


    public RootExecutionResultNode(List<ExecutionResultNode> children, List<GraphQLError> errors) {
        super(null, null, children, errors);
    }

    public RootExecutionResultNode(List<ExecutionResultNode> children) {
        super(null, null, children, Collections.emptyList());
    }

    @Override
    public ExecutionStepInfo getExecutionStepInfo() {
        return assertShouldNeverHappen("not supported at root node");
    }

    @Override
    public ResolvedValue getResolvedValue() {
        return assertShouldNeverHappen("not supported at root node");
    }

    @Override
    public RootExecutionResultNode withNewChildren(List<ExecutionResultNode> children) {
        return new RootExecutionResultNode(children, getErrors());
    }

    @Override
    public ExecutionResultNode withNewResolvedValue(ResolvedValue resolvedValue) {
        return assertShouldNeverHappen("not supported at root node");
    }

    @Override
    public ExecutionResultNode withNewExecutionStepInfo(ExecutionStepInfo executionStepInfo) {
        return assertShouldNeverHappen("not supported at root node");
    }

    @Override
    public ExecutionResultNode withNewErrors(List<GraphQLError> errors) {
        return new RootExecutionResultNode(getChildren(), new ArrayList<>(errors));
    }
}
