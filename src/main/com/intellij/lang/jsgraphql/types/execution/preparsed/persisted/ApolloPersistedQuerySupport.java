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

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.PublicApi;

import java.util.Map;
import java.util.Optional;

/**
 * This persisted query support class supports the Apollo scheme where the persisted
 * query id is in {@link ExecutionInput#getExtensions()}.
 * <p>
 * You need to provide a {@link PersistedQueryCache} cache implementation
 * as the backing cache.
 * <p>
 * See <a href="https://www.apollographql.com/docs/apollo-server/performance/apq/">Apollo Persisted Queries</a>
 * <p>
 * The Apollo client sends a hash of the persisted query in the input extensions in the following form
 * <pre>
 *     {
 *      "extensions":{
 *       "persistedQuery":{
 *        "version":1,
 *        "sha256Hash":"fcf31818e50ac3e818ca4bdbc433d6ab73176f0b9d5f9d5ad17e200cdab6fba4"
 *      }
 *    }
 *  }
 * </pre>
 *
 * @see ExecutionInput#getExtensions()
 */
@PublicApi
public class ApolloPersistedQuerySupport extends PersistedQuerySupport {

  public ApolloPersistedQuerySupport(PersistedQueryCache persistedQueryCache) {
    super(persistedQueryCache);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Optional<Object> getPersistedQueryId(ExecutionInput executionInput) {
    Map<String, Object> extensions = executionInput.getExtensions();
    Map<String, Object> persistedQuery = (Map<String, Object>)extensions.get("persistedQuery");
    if (persistedQuery != null) {
      Object sha256Hash = persistedQuery.get("sha256Hash");
      return Optional.ofNullable(sha256Hash);
    }
    return Optional.empty();
  }
}
