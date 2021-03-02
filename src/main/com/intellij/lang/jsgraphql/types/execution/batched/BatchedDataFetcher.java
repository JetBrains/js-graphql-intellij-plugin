package com.intellij.lang.jsgraphql.types.execution.batched;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;

/**
 * See {@link Batched}.
 * @deprecated This has been deprecated in favour of using {@link com.intellij.lang.jsgraphql.types.execution.AsyncExecutionStrategy} and {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation}
 */
@Deprecated
@PublicApi
public interface BatchedDataFetcher extends DataFetcher {
    // Marker interface
}
