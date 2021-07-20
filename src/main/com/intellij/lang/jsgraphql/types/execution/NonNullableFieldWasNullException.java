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

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;
import static com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil.simplePrint;

/**
 * See (http://facebook.github.io/graphql/#sec-Errors-and-Non-Nullability), but if a non nullable field
 * actually resolves to a null value and the parent type is nullable then the parent must in fact become null
 * so we use exceptions to indicate this special case
 */
@Internal
public class NonNullableFieldWasNullException extends RuntimeException {

    private final ExecutionStepInfo executionStepInfo;
    private final ResultPath path;


    public NonNullableFieldWasNullException(ExecutionStepInfo executionStepInfo, ResultPath path) {
        super(
                mkMessage(assertNotNull(executionStepInfo),
                        assertNotNull(path))
        );
        this.executionStepInfo = executionStepInfo;
        this.path = path;
    }

    public NonNullableFieldWasNullException(NonNullableFieldWasNullException previousException) {
        super(
                mkMessage(
                        assertNotNull(previousException.executionStepInfo.getParent()),
                        assertNotNull(previousException.executionStepInfo.getParent().getPath())
                ),
                previousException
        );
        this.executionStepInfo = previousException.executionStepInfo.getParent();
        this.path = previousException.executionStepInfo.getParent().getPath();
    }


    private static String mkMessage(ExecutionStepInfo executionStepInfo, ResultPath path) {
        GraphQLType unwrappedTyped = executionStepInfo.getUnwrappedNonNullType();
        if (executionStepInfo.hasParent()) {
            GraphQLType unwrappedParentType = executionStepInfo.getParent().getUnwrappedNonNullType();
            return String.format(
                    "The field at path '%s' was declared as a non null type, but the code involved in retrieving" +
                            " data has wrongly returned a null value.  The graphql specification requires that the" +
                            " parent field be set to null, or if that is non nullable that it bubble up null to its parent and so on." +
                            " The non-nullable type is '%s' within parent type '%s'",
                    path, simplePrint(unwrappedTyped), simplePrint(unwrappedParentType));
        } else {
            return String.format(
                    "The field at path '%s' was declared as a non null type, but the code involved in retrieving" +
                            " data has wrongly returned a null value.  The graphql specification requires that the" +
                            " parent field be set to null, or if that is non nullable that it bubble up null to its parent and so on." +
                            " The non-nullable type is '%s'",
                    path, simplePrint(unwrappedTyped));
        }
    }

    public ExecutionStepInfo getExecutionStepInfo() {
        return executionStepInfo;
    }

    public ResultPath getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
