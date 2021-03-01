package com.intellij.lang.jsgraphql.types.relay;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphqlErrorHelper;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.List;

import static com.intellij.lang.jsgraphql.types.ErrorType.DataFetchingException;

@Internal
public class InvalidPageSizeException extends RuntimeException implements GraphQLError {

    InvalidPageSizeException(String message) {
        this(message, null);
    }

    InvalidPageSizeException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return DataFetchingException;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }

}
