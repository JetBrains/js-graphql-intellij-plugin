package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphqlErrorHelper;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

import java.util.List;

/**
 * This is the base error that indicates that a non null field value was in fact null.
 *
 * @see com.intellij.lang.jsgraphql.types.execution.NonNullableFieldWasNullException for details
 */
@Internal
public class NonNullableFieldWasNullError implements GraphQLError {

    private final String message;
    private final List<Object> path;

    public NonNullableFieldWasNullError(NonNullableFieldWasNullException exception) {
        this.message = exception.getMessage();
        this.path = exception.getPath().toList();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.NullValueInNonNullableField;
    }

    @Override
    public String toString() {
        return "NonNullableFieldWasNullError{" +
                "message='" + message + '\'' +
                ", path=" + path +
                '}';
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
