package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphqlErrorException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;

@PublicApi
public class CoercingParseLiteralException extends GraphqlErrorException {

    public CoercingParseLiteralException() {
        this(newCoercingParseLiteralException());
    }

    public CoercingParseLiteralException(String message) {
        this(newCoercingParseLiteralException().message(message));
    }

    public CoercingParseLiteralException(String message, Throwable cause) {
        this(newCoercingParseLiteralException().message(message).cause(cause));
    }

    public CoercingParseLiteralException(String message, Throwable cause, SourceLocation sourceLocation) {
        this(newCoercingParseLiteralException().message(message).cause(cause).sourceLocation(sourceLocation));
    }

    public CoercingParseLiteralException(Throwable cause) {
        this(newCoercingParseLiteralException().cause(cause));
    }

    private CoercingParseLiteralException(Builder builder) {
        super(builder);
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.ValidationError;
    }

    public static Builder newCoercingParseLiteralException() {
        return new Builder();
    }

    public static class Builder extends BuilderBase<Builder, CoercingParseLiteralException> {
        public CoercingParseLiteralException build() {
            return new CoercingParseLiteralException(this);
        }
    }
}
