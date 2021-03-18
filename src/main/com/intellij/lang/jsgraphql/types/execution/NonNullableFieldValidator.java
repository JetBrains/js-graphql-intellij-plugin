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

/**
 * This will check that a value is non null when the type definition says it must be and it will throw {@link NonNullableFieldWasNullException}
 * if this is not the case.
 *
 * See: http://facebook.github.io/graphql/#sec-Errors-and-Non-Nullability
 */
@Internal
public class NonNullableFieldValidator {

    private final ExecutionContext executionContext;
    private final ExecutionStepInfo executionStepInfo;

    public NonNullableFieldValidator(ExecutionContext executionContext, ExecutionStepInfo executionStepInfo) {
        this.executionContext = executionContext;
        this.executionStepInfo = executionStepInfo;
    }

    /**
     * Called to check that a value is non null if the type requires it to be non null
     *
     * @param path   the path to this place
     * @param result the result to check
     * @param <T>    the type of the result
     *
     * @return the result back
     *
     * @throws NonNullableFieldWasNullException if the value is null but the type requires it to be non null
     */
    public <T> T validate(ResultPath path, T result) throws NonNullableFieldWasNullException {
        if (result == null) {
            if (executionStepInfo.isNonNullType()) {
                // see http://facebook.github.io/graphql/#sec-Errors-and-Non-Nullability
                //
                //    > If the field returns null because of an error which has already been added to the "errors" list in the response,
                //    > the "errors" list must not be further affected. That is, only one error should be added to the errors list per field.
                //
                // We interpret this to cover the null field path only.  So here we use the variant of addError() that checks
                // for the current path already.
                //
                // Other places in the code base use the addError() that does not care about previous errors on that path being there.
                //
                // We will do this until the spec makes this more explicit.
                //
                NonNullableFieldWasNullException nonNullException = new NonNullableFieldWasNullException(executionStepInfo, path);
                executionContext.addError(new NonNullableFieldWasNullError(nonNullException), path);
                throw nonNullException;
            }
        }
        return result;
    }

}
