package com.intellij.lang.jsgraphql.types.execution.preparsed.persisted;

import com.intellij.lang.jsgraphql.types.ErrorClassification;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An exception that indicates the query id is not valid and can be found ever in cache
 */
@PublicApi
public class PersistedQueryNotFound extends RuntimeException implements ErrorClassification {
    private final Object persistedQueryId;

    public PersistedQueryNotFound(Object persistedQueryId) {
        this.persistedQueryId = persistedQueryId;
    }

    @Override
    public String getMessage() {
        return "PersistedQueryNotFound";
    }

    public Object getPersistedQueryId() {
        return persistedQueryId;
    }

    @Override
    public String toString() {
        return "PersistedQueryNotFound";
    }

    public Map<String, Object> getExtensions() {
        LinkedHashMap<String, Object> extensions = new LinkedHashMap<>();
        extensions.put("persistedQueryId", persistedQueryId);
        extensions.put("generatedBy", "graphql-java");
        return extensions;
    }
}
