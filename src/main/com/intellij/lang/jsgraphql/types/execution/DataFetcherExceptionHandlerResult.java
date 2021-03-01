package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * The result object for {@link graphql.execution.DataFetcherExceptionHandler}s
 */
@PublicApi
public class DataFetcherExceptionHandlerResult {

    private final List<GraphQLError> errors;

    private DataFetcherExceptionHandlerResult(Builder builder) {
        this.errors = builder.errors;
    }

    public List<GraphQLError> getErrors() {
        return errors;
    }

    public static Builder newResult() {
        return new Builder();
    }

    public static Builder newResult(GraphQLError error) {
        return new Builder().error(error);
    }

    public static class Builder {

        private final List<GraphQLError> errors = new ArrayList<>();

        public Builder errors(List<GraphQLError> errors) {
            this.errors.addAll(assertNotNull(errors));
            return this;
        }

        public Builder error(GraphQLError error) {
            errors.add(assertNotNull(error));
            return this;
        }

        public DataFetcherExceptionHandlerResult build() {
            return new DataFetcherExceptionHandlerResult(this);
        }
    }
}
