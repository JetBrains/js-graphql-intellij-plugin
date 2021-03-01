package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

import java.util.Map;

/**
 * The parameters available to {@link DataFetcherExceptionHandler}s
 */
@PublicApi
public class DataFetcherExceptionHandlerParameters {

    private final DataFetchingEnvironment dataFetchingEnvironment;
    private final Throwable exception;

    private DataFetcherExceptionHandlerParameters(Builder builder) {
        this.exception = builder.exception;
        this.dataFetchingEnvironment = builder.dataFetchingEnvironment;
    }

    public Throwable getException() {
        return exception;
    }

    public ResultPath getPath() {
        return dataFetchingEnvironment.getExecutionStepInfo().getPath();
    }

    public DataFetchingEnvironment getDataFetchingEnvironment() {
        return dataFetchingEnvironment;
    }

    public MergedField getField() {
        return dataFetchingEnvironment.getMergedField();
    }

    public GraphQLFieldDefinition getFieldDefinition() {
        return dataFetchingEnvironment.getFieldDefinition();
    }

    public Map<String, Object> getArgumentValues() {
        return dataFetchingEnvironment.getArguments();
    }

    public SourceLocation getSourceLocation() {
        return getField().getSingleField().getSourceLocation();
    }

    public static Builder newExceptionParameters() {
        return new Builder();
    }

    public static class Builder {
        DataFetchingEnvironment dataFetchingEnvironment;
        Throwable exception;

        private Builder() {
        }

        public Builder dataFetchingEnvironment(DataFetchingEnvironment dataFetchingEnvironment) {
            this.dataFetchingEnvironment = dataFetchingEnvironment;
            return this;
        }

        public Builder exception(Throwable exception) {
            this.exception = exception;
            return this;
        }

        public DataFetcherExceptionHandlerParameters build() {
            return new DataFetcherExceptionHandlerParameters(this);
        }
    }
}
