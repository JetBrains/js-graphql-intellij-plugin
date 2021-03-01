package com.intellij.lang.jsgraphql.types;


/**
 * All the errors in graphql belong to one of these categories
 */
@PublicApi
public enum ErrorType implements ErrorClassification {
    InvalidSyntax,
    ValidationError,
    DataFetchingException,
    NullValueInNonNullableField,
    OperationNotSupported,
    ExecutionAborted
}
