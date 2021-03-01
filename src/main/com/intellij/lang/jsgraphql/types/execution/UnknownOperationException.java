package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.*;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.List;

/**
 * This is thrown if multiple operations are defined in the query and
 * the operation name is missing or there is no matching operation name
 * contained in the GraphQL query.
 */
@PublicApi
public class UnknownOperationException extends GraphQLException implements GraphQLError {
    public UnknownOperationException(String message) {
        super(message);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.ValidationError;
    }
}
