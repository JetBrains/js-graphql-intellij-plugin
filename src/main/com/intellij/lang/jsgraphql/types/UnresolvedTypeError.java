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
package com.intellij.lang.jsgraphql.types;

import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.ResultPath;
import com.intellij.lang.jsgraphql.types.execution.UnresolvedTypeException;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.simplePrint;
import static java.lang.String.format;

@PublicApi
public class UnresolvedTypeError implements GraphQLError {

    private final String message;
    private final List<Object> path;
    private final UnresolvedTypeException exception;

    public UnresolvedTypeError(ResultPath path, ExecutionStepInfo info,
                               UnresolvedTypeException exception) {
        this.path = assertNotNull(path).toList();
        this.exception = assertNotNull(exception);
        this.message = mkMessage(path, exception, assertNotNull(info));
    }

    private String mkMessage(ResultPath path, UnresolvedTypeException exception, ExecutionStepInfo info) {
        return format("Can't resolve '%s'. Abstract type '%s' must resolve to an Object type at runtime for field '%s.%s'. %s",
                path,
                exception.getInterfaceOrUnionType().getName(),
                simplePrint(info.getParent().getUnwrappedNonNullType()),
                info.getFieldDefinition().getName(),
                exception.getMessage());
    }

    public UnresolvedTypeException getException() {
        return exception;
    }


    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "UnresolvedTypeError{" +
                "path=" + path +
                ", exception=" + exception +
                '}';
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }
}
