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

import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.GraphQLNamedOutputType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil;

/**
 * This is thrown if a {@link graphql.schema.TypeResolver} fails to give back a concrete type
 * or provides a type that doesn't implement the given interface or union.
 */
@PublicApi
public class UnresolvedTypeException extends GraphQLException {

    private final GraphQLNamedOutputType interfaceOrUnionType;

    /**
     * Constructor to use a custom error message
     * for an error that happened during type resolution.
     *
     * @param message              custom error message.
     * @param interfaceOrUnionType expected type.
     */
    public UnresolvedTypeException(String message, GraphQLNamedOutputType interfaceOrUnionType) {
        super(message);
        this.interfaceOrUnionType = interfaceOrUnionType;
    }

    public UnresolvedTypeException(GraphQLNamedOutputType interfaceOrUnionType) {
        this("Could not determine the exact type of '" + interfaceOrUnionType.getName() + "'", interfaceOrUnionType);
    }

    public UnresolvedTypeException(GraphQLNamedOutputType interfaceOrUnionType, GraphQLType providedType) {
        this("Runtime Object type '" + GraphQLTypeUtil.simplePrint(providedType) + "' is not a possible type for "
                + "'" + interfaceOrUnionType.getName() + "'.", interfaceOrUnionType);
    }

    public GraphQLNamedOutputType getInterfaceOrUnionType() {
        return interfaceOrUnionType;
    }

}
