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
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphqlErrorBuilder;
import com.intellij.lang.jsgraphql.types.PublicSpi;
import com.intellij.lang.jsgraphql.types.execution.preparsed.PreparsedDocumentEntry;
import com.intellij.lang.jsgraphql.types.execution.preparsed.PreparsedDocumentProvider;

import java.util.Optional;
import java.util.function.Function;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * This abstract class forms the basis for persistent query support.  Derived classes
 * need to implement the method to work out the query id and you also need
 * a {@link PersistedQueryCache} implementation.
 *
 * @see graphql.execution.preparsed.PreparsedDocumentProvider
 * @see graphql.GraphQL.Builder#preparsedDocumentProvider(graphql.execution.preparsed.PreparsedDocumentProvider)
 */
@PublicSpi
public abstract class PersistedQuerySupport implements PreparsedDocumentProvider {

  /**
   * In order for {@link ExecutionInput#getQuery()} to never be null, use this to mark
   * them so that invariant can be satisfied while assuming that the persisted query id is elsewhere
   */
  public static final String PERSISTED_QUERY_MARKER = "PersistedQueryMarker";

  private final PersistedQueryCache persistedQueryCache;

  public PersistedQuerySupport(PersistedQueryCache persistedQueryCache) {
    this.persistedQueryCache = assertNotNull(persistedQueryCache);
  }

  @Override
  public PreparsedDocumentEntry getDocument(ExecutionInput executionInput,
                                            Function<ExecutionInput, PreparsedDocumentEntry> parseAndValidateFunction) {
    Optional<Object> queryIdOption = getPersistedQueryId(executionInput);
    assertNotNull(queryIdOption, () -> String.format("The class %s MUST return a non null optional query id", this.getClass().getName()));

    try {
      if (queryIdOption.isPresent()) {
        Object persistedQueryId = queryIdOption.get();
        return persistedQueryCache.getPersistedQueryDocument(persistedQueryId, executionInput, (queryText) -> {
          // we have a miss and they gave us nothing - bah!
          if (queryText == null || queryText.trim().length() == 0) {
            throw new PersistedQueryNotFound(persistedQueryId);
          }
          ExecutionInput newEI = executionInput.transform(builder -> builder.query(queryText));
          return parseAndValidateFunction.apply(newEI);
        });
      }
      // ok there is no query id - we assume the query is indeed ready to go as is - ie its not a persisted query
      return parseAndValidateFunction.apply(executionInput);
    }
    catch (PersistedQueryNotFound e) {
      return mkMissingError(e);
    }
  }

  /**
   * This method is required for concrete types to work out the query id (often a hash) that should be used to look
   * up the persisted query in the cache.
   *
   * @param executionInput the execution input
   * @return an optional id of the persisted query
   */
  abstract protected Optional<Object> getPersistedQueryId(ExecutionInput executionInput);

  /**
   * Allows you to customize the graphql error that is sent back on a missing persistend query
   *
   * @param persistedQueryNotFound the missing persistent query exception
   * @return a PreparsedDocumentEntry that holds an error
   */
  protected PreparsedDocumentEntry mkMissingError(PersistedQueryNotFound persistedQueryNotFound) {
    GraphQLError gqlError = GraphqlErrorBuilder.newError()
      .errorType(persistedQueryNotFound).message(persistedQueryNotFound.getMessage())
      .extensions(persistedQueryNotFound.getExtensions()).build();
    return new PreparsedDocumentEntry(gqlError);
  }
}
