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
package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.*;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static java.util.Collections.emptyList;

/**
 * This Exception indicates that the current execution should be aborted.
 */
@PublicApi
public class AbortExecutionException extends GraphQLException implements GraphQLError {

    private final List<GraphQLError> underlyingErrors;

    public AbortExecutionException() {
        this.underlyingErrors = emptyList();
    }

    public AbortExecutionException(Collection<GraphQLError> underlyingErrors) {
        this.underlyingErrors = new ArrayList<>(assertNotNull(underlyingErrors));
    }

    public AbortExecutionException(String message) {
        super(message);
        this.underlyingErrors = emptyList();
    }

    public AbortExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.underlyingErrors = emptyList();
    }

    public AbortExecutionException(Throwable cause) {
        super(cause);
        this.underlyingErrors = emptyList();
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.ExecutionAborted;
    }

    /**
     * @return a list of underlying errors, which may be empty
     */
    public List<GraphQLError> getUnderlyingErrors() {
        return underlyingErrors;
    }

    /**
     * This is useful for turning this abort signal into an execution result which
     * is an error state with the underlying errors in it.
     *
     * @return an execution result with the errors from this exception
     */
    public ExecutionResult toExecutionResult() {
        if (!this.getUnderlyingErrors().isEmpty()) {
            return new ExecutionResultImpl(this.getUnderlyingErrors());
        }

        return new ExecutionResultImpl(this);
    }
}
