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
package com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters;

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

/**
 * Parameters sent to {@link Instrumentation} methods
 */
@PublicApi
public class InstrumentationValidationParameters extends InstrumentationExecutionParameters {
    private final Document document;

    public InstrumentationValidationParameters(ExecutionInput executionInput, Document document, GraphQLSchema schema, InstrumentationState instrumentationState) {
        super(executionInput, schema, instrumentationState);
        this.document = document;
    }

    /**
     * Returns a cloned parameters object with the new state
     *
     * @param instrumentationState the new state for this parameters object
     *
     * @return a new parameters object with the new state
     */
    @Override
    public InstrumentationValidationParameters withNewState(InstrumentationState instrumentationState) {
        return new InstrumentationValidationParameters(
                this.getExecutionInput(), document, getSchema(), instrumentationState);
    }


    public Document getDocument() {
        return document;
    }
}
