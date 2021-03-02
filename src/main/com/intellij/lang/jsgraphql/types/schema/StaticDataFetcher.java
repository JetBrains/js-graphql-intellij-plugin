package com.intellij.lang.jsgraphql.types.schema;


import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.TrivialDataFetcher;

/**
 * A {@link com.intellij.lang.jsgraphql.types.schema.DataFetcher} that always returns the same value
 */
@PublicApi
public class StaticDataFetcher implements DataFetcher, TrivialDataFetcher {


    private final Object value;

    public StaticDataFetcher(Object value) {
        this.value = value;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        return value;
    }

}
