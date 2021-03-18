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
import com.intellij.lang.jsgraphql.types.PublicSpi;

import java.util.List;

/**
 * This pluggable interface allows you to validate the fields and their argument inputs before query execution.
 *
 * You will be called with fields and their arguments expanded out ready for execution and you can check business logic
 * concerns like the lengths of input objects (eg an input string cant be longer than say 255 chars) or that the
 * input objects have a certain shape that is required for this query.
 *
 * You are only called once with all the field information expanded out for you.  This allows you to set up cross field business rules,
 * for example if field argument X has a value then field Y argument must also have a value say.
 *
 * @see FieldValidationEnvironment
 * @see SimpleFieldValidation
 */
@PublicSpi
public interface FieldValidation {

    /**
     * This is called to validate the fields and their arguments
     *
     * @param validationEnvironment the validation environment
     *
     * @return a list of errors.  If this is non empty then the query will not execute.
     */
    List<GraphQLError> validateFields(FieldValidationEnvironment validationEnvironment);
}
