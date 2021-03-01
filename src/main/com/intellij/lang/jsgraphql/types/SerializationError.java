package com.intellij.lang.jsgraphql.types;


import com.intellij.lang.jsgraphql.types.execution.ResultPath;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.schema.CoercingSerializeException;

import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

@PublicApi
public class SerializationError implements GraphQLError {

    private final String message;
    private final List<Object> path;
    private final CoercingSerializeException exception;

    public SerializationError(ResultPath path, CoercingSerializeException exception) {
        this.path = assertNotNull(path).toList();
        this.exception = assertNotNull(exception);
        this.message = mkMessage(path, exception);
    }

    private String mkMessage(ResultPath path, CoercingSerializeException exception) {
        return format("Can't serialize value (%s) : %s", path, exception.getMessage());
    }

    public CoercingSerializeException getException() {
        return exception;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return exception.getLocations();
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public List<Object> getPath() {
        return path;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return exception.getExtensions();
    }

    @Override
    public String toString() {
        return "SerializationError{" +
                "path=" + path +
                ", exception=" + exception +
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
