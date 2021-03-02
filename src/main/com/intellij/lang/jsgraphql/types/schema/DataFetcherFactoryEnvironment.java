package com.intellij.lang.jsgraphql.types.schema;

import com.intellij.lang.jsgraphql.types.PublicApi;

/**
 * This is passed to a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcherFactory} when it is invoked to
 * get a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher}
 */
@PublicApi
public class DataFetcherFactoryEnvironment {
    private final GraphQLFieldDefinition fieldDefinition;

    DataFetcherFactoryEnvironment(GraphQLFieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    /**
     * @return the field that needs a {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher}
     */
    public GraphQLFieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public static Builder newDataFetchingFactoryEnvironment() {
        return new Builder();
    }

    static class Builder {
        GraphQLFieldDefinition fieldDefinition;

        public Builder fieldDefinition(GraphQLFieldDefinition fieldDefinition) {
            this.fieldDefinition = fieldDefinition;
            return this;
        }

        public DataFetcherFactoryEnvironment build() {
            return new DataFetcherFactoryEnvironment(fieldDefinition);
        }
    }
}
