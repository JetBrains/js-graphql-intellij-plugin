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

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.ResultPath;

import java.util.List;
import java.util.Map;

/**
 * This contains all of the field and their arguments for a given query.  The method
 * {@link #getFieldsByPath()} will be where most of the useful validation information is
 * contained.  It also gives you a helper to make validation error messages.
 *
 * One thing to note is that because queries can have repeating fragments, the same
 * logical field can appear multiple times with different input values.  That is
 * why {@link #getFieldsByPath()} returns a list of fields and their arguments.
 * if you don't have fragments then the list will be of size 1
 *
 * @see FieldAndArguments
 */
@PublicApi
public interface FieldValidationEnvironment {

    /**
     * @return the schema in play
     */
    ExecutionContext getExecutionContext();

    /**
     * @return a list of {@link FieldAndArguments}
     */
    List<FieldAndArguments> getFields();

    /**
     * @return a map of field paths to {@link FieldAndArguments}
     */
    Map<ResultPath, List<FieldAndArguments>> getFieldsByPath();

    /**
     * This helper method allows you to make error messages to be passed back out in case of validation failure.  Note you
     * don't NOT have to use this helper.  Any implementation of {@link GraphQLError} is valid
     *
     * @param msg the error message
     *
     * @return a graphql error
     */
    GraphQLError mkError(String msg);

    /**
     * This helper method allows you to make error messages to be passed back out in case of validation failure.  Note you
     * don't NOT have to use this helper.  Any implementation of {@link GraphQLError} is valid
     *
     * @param msg               the error message
     * @param fieldAndArguments the field in error
     *
     * @return a graphql error
     */
    GraphQLError mkError(String msg, FieldAndArguments fieldAndArguments);
}
