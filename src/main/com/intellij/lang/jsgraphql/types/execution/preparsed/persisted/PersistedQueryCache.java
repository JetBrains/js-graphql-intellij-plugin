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
import com.intellij.lang.jsgraphql.types.PublicSpi;
import com.intellij.lang.jsgraphql.types.execution.preparsed.PreparsedDocumentEntry;

/**
 * This interface is used to abstract an actual cache that can cache parsed persistent queries.
 */
@PublicSpi
public interface PersistedQueryCache {

  /**
   * This is called to get a persisted query from cache.
   * <p>
   * If its present in cache then  it must return a PreparsedDocumentEntry where {@link graphql.execution.preparsed.PreparsedDocumentEntry#getDocument()}
   * is already parsed and validated.  This will be passed onto the graphql engine as is.
   * <p>
   * If its a valid query id but its no present in cache, (cache miss) then you need to call back the "onCacheMiss" function with associated query text.
   * This will be compiled and validated by the graphql engine and the a PreparsedDocumentEntry will be passed back ready for you to cache it.
   * <p>
   * If its not a valid query id then throw a {@link graphql.execution.preparsed.persisted.PersistedQueryNotFound} to indicate this.
   *
   * @param persistedQueryId the persisted query id
   * @param executionInput   the original execution input
   * @param onCacheMiss      the call back should it be a valid query id but its not currently not in the cache
   * @return a parsed and validated PreparsedDocumentEntry where {@link graphql.execution.preparsed.PreparsedDocumentEntry#getDocument()} is set
   * @throws graphql.execution.preparsed.persisted.PersistedQueryNotFound if the query id is not know at all and you have no query text
   */
  PreparsedDocumentEntry getPersistedQueryDocument(Object persistedQueryId,
                                                   ExecutionInput executionInput,
                                                   PersistedQueryCacheMiss onCacheMiss) throws PersistedQueryNotFound;
}
