package com.intellij.lang.jsgraphql.types;


@PublicApi
public class AssertException extends GraphQLException {

    public AssertException(String message) {
        super(message);
    }
}
