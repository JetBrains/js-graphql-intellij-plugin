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

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ResultPath;
import com.intellij.lang.jsgraphql.types.language.Field;
import com.intellij.lang.jsgraphql.types.schema.GraphQLCompositeType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

import java.util.Map;

/**
 * This represents a field and its arguments that may be validated.
 */
@PublicApi
public interface FieldAndArguments {

    /**
     * @return the field in play
     */
    Field getField();

    /**
     * @return the runtime type definition of the field
     */
    GraphQLFieldDefinition getFieldDefinition();

    /**
     * @return the containing type of the field
     */
    GraphQLCompositeType getParentType();

    /**
     * @return the parent arguments or null if there is no parent
     */
    FieldAndArguments getParentFieldAndArguments();

    /**
     * @return the path to this field
     */
    ResultPath getPath();

    /**
     * This will be a map of argument names to argument values.  This will contain any variables transferred
     * along with any default values ready for execution.  This is what you use to do most of your validation against
     *
     * @return a map of argument names to values
     */
    Map<String, Object> getArgumentValuesByName();

    /**
     * This will return the named field argument value and cast it to the desired type.
     *
     * @param argumentName the name of the argument
     * @param <T>          the type of the underlying value object
     *
     * @return a cast object of type T
     */
    @SuppressWarnings("TypeParameterUnusedInFormals")
    <T> T getArgumentValue(String argumentName);
}
