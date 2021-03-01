package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.ErrorClassification;
import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphqlErrorException;
import com.intellij.lang.jsgraphql.types.PublicApi;

@PublicApi
public class CoercingSerializeException extends GraphqlErrorException {

    public CoercingSerializeException() {
        this(newCoercingSerializeException());
    }

    public CoercingSerializeException(String message) {
        this(newCoercingSerializeException().message(message));
    }

    public CoercingSerializeException(String message, Throwable cause) {
        this(newCoercingSerializeException().message(message).cause(cause));
    }

    public CoercingSerializeException(Throwable cause) {
        this(newCoercingSerializeException().cause(cause));
    }

    private CoercingSerializeException(Builder builder) {
        super(builder);
    }

    @Override
    public ErrorClassification getErrorType() {
        return ErrorType.DataFetchingException;
    }

    public static Builder newCoercingSerializeException() {
        return new Builder();
    }

    public static class Builder extends BuilderBase<Builder, CoercingSerializeException> {
        public CoercingSerializeException build() {
            return new CoercingSerializeException(this);
        }
    }
}
