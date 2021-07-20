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
package com.intellij.lang.jsgraphql.types.execution.instrumentation.fieldvalidation;

import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.AbortExecutionException;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;

import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * This {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation} allows you to validate the fields
 * of the query before the query is executed.  You need to provide an implementation of
 * {@link FieldValidation} that is called to validate fields.  If it returns errors
 * then the query execution will be aborted and the errors will be returned
 * in the execution result
 *
 * @see FieldValidation
 */
@PublicApi
public class FieldValidationInstrumentation extends SimpleInstrumentation {

    private final FieldValidation fieldValidation;

    /**
     * Your field validation will be called before query execution
     *
     * @param fieldValidation the field validation to call
     */
    public FieldValidationInstrumentation(FieldValidation fieldValidation) {
        this.fieldValidation = assertNotNull(fieldValidation);
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {

        List<GraphQLError> errors = FieldValidationSupport.validateFieldsAndArguments(fieldValidation, parameters.getExecutionContext());
        if (errors != null && !errors.isEmpty()) {
            throw new AbortExecutionException(errors);
        }
        return super.beginExecuteOperation(parameters);
    }
}
