package com.intellij.lang.jsgraphql.types.execution.batched;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;

/**
 * See {@link Batched}.
 * @deprecated This has been deprecated in favour of using {@link graphql.execution.AsyncExecutionStrategy} and {@link graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation}
 */
@Deprecated
@PublicApi
public interface BatchedDataFetcher extends DataFetcher {
    // Marker interface
}
