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
package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.Map;

/**
 * A {@link com.intellij.lang.jsgraphql.types.schema.SelectedField} represents a field that occurred in a query selection set during
 * execution and they are returned from using the {@link com.intellij.lang.jsgraphql.types.schema.DataFetchingFieldSelectionSet}
 * interface returned via {@link DataFetchingEnvironment#getSelectionSet()}
 */
@PublicApi
public interface SelectedField {
    /**
     * @return the simple name of the selected field
     */
    String getName();

    /**
     * The selected field has a simple qualified name which is the path of field names to it.
     * For example `payments/amount`.
     *
     * @return the simple qualified name of the selected field
     */
    String getQualifiedName();

    /**
     * The selected field has a more complex type qualified name which is the path of field names to it
     * as well as the object type of the parent.  For example `Invoice.payments/Payment.amount`
     *
     * @return the fully qualified name of the selected field
     */
    String getFullyQualifiedName();

    /**
     * @return the containing object type of this selected field
     */
    GraphQLObjectType getObjectType();

    /**
     * @return the field runtime definition
     */
    GraphQLFieldDefinition getFieldDefinition();

    /**
     * @return a map of the arguments to this selected field
     */
    Map<String, Object> getArguments();

    /**
     * @return the level of the selected field within the query
     */
    int getLevel();

    /**
     * @return whether the field is conditionally present.
     */
    boolean isConditional();

    /**
     * @return the alias of the selected field or null if not alias was used
     */
    String getAlias();

    /**
     * The result key is either the field query alias OR the field name in that preference order
     *
     * @return the result key of the selected field
     */
    String getResultKey();

    /**
     * This will return the parent of the selected field OR null if there is no single parent, it that field
     * was a top level field OR the parent was a non concrete field.
     *
     * @return the fields selected parent or null if there is not one
     */
    SelectedField getParentField();

    /**
     * @return a sub selection set (if it has any)
     */
    DataFetchingFieldSelectionSet getSelectionSet();
}
