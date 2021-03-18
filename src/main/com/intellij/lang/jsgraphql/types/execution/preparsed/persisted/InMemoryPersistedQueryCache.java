/*
    The MIT License (MIT)

    Copyright (c) 2015 Andreas Marek and Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
    (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
    publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
    so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
    OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
    CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.intellij.lang.jsgraphql.types.execution.preparsed.persisted;

import com.intellij.lang.jsgraphql.types.Assert;
import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.preparsed.PreparsedDocumentEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A PersistedQueryCache that is just an in memory map of known queries.
 */
@PublicApi
public class InMemoryPersistedQueryCache implements PersistedQueryCache {

    private final Map<Object, PreparsedDocumentEntry> cache = new ConcurrentHashMap<>();
    private final Map<Object, String> knownQueries;

    public InMemoryPersistedQueryCache(Map<Object, String> knownQueries) {
        this.knownQueries = Assert.assertNotNull(knownQueries);
    }

    public Map<Object, String> getKnownQueries() {
        return knownQueries;
    }

    @Override
    public PreparsedDocumentEntry getPersistedQueryDocument(Object persistedQueryId, ExecutionInput executionInput, PersistedQueryCacheMiss onCacheMiss) throws PersistedQueryNotFound {
        return cache.compute(persistedQueryId, (k, v) -> {
            if (v != null) {
                return v;
            }
            String queryText = knownQueries.get(persistedQueryId);
            if (queryText == null) {
                throw new PersistedQueryNotFound(persistedQueryId);
            }
            return onCacheMiss.apply(queryText);
        });
    }

    public static Builder newInMemoryPersistedQueryCache() {
        return new Builder();
    }

    public static class Builder {
        private final Map<Object, String> knownQueries = new HashMap<>();

        public Builder addQuery(Object key, String queryText) {
            knownQueries.put(key, queryText);
            return this;
        }

        public InMemoryPersistedQueryCache build() {
            return new InMemoryPersistedQueryCache(knownQueries);
        }
    }
}
