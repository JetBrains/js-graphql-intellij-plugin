package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.Collections;
import java.util.List;

/**
 * This is thrown if a query is attempting to perform an operation not defined in the GraphQL schema
 */
@PublicApi
public class MissingRootTypeException extends GraphQLException implements GraphQLError {
    private List<SourceLocation> sourceLocations;

    public MissingRootTypeException(String message, SourceLocation sourceLocation) {
        super(message);
        this.sourceLocations = sourceLocation == null ? null : Collections.singletonList(sourceLocation);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return sourceLocations;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.OperationNotSupported;
    }
}
