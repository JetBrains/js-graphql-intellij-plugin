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
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.MergedField;
import com.intellij.lang.jsgraphql.types.execution.MergedSelectionSet;

import java.util.Map;


/**
 * A map from name to List of Field representing the actual sub selections (during execution) of a Field with Fragments
 * evaluated and conditional directives considered.
 */
@Internal
public class FieldSubSelection {

    private final Object source;
    private final Object localContext;
    // the type of this must be objectType and is the parent executionStepInfo for all mergedSelectionSet
    private final ExecutionStepInfo executionInfo;
    private final MergedSelectionSet mergedSelectionSet;

    private FieldSubSelection(Builder builder) {
        this.source = builder.source;
        this.localContext = builder.localContext;
        this.executionInfo = builder.executionInfo;
        this.mergedSelectionSet = builder.mergedSelectionSet;
    }

    public Object getSource() {
        return source;
    }

    public Object getLocalContext() {
        return localContext;
    }

    public Map<String, MergedField> getSubFields() {
        return mergedSelectionSet.getSubFields();
    }

    public MergedSelectionSet getMergedSelectionSet() {
        return mergedSelectionSet;
    }

    public ExecutionStepInfo getExecutionStepInfo() {
        return executionInfo;
    }

    @Override
    public String toString() {
        return "FieldSubSelection{" +
                "source=" + source +
                ", executionInfo=" + executionInfo +
                ", mergedSelectionSet" + mergedSelectionSet +
                '}';
    }

    public static Builder newFieldSubSelection() {
        return new Builder();
    }

    public static class Builder {
        private Object source;
        private Object localContext;
        private ExecutionStepInfo executionInfo;
        private MergedSelectionSet mergedSelectionSet;

        public Builder source(Object source) {
            this.source = source;
            return this;
        }

        public Builder localContext(Object localContext) {
            this.localContext = localContext;
            return this;
        }

        public Builder executionInfo(ExecutionStepInfo executionInfo) {
            this.executionInfo = executionInfo;
            return this;
        }

        public Builder mergedSelectionSet(MergedSelectionSet mergedSelectionSet) {
            this.mergedSelectionSet = mergedSelectionSet;
            return this;
        }

        public FieldSubSelection build() {
            return new FieldSubSelection(this);
        }


    }

}
