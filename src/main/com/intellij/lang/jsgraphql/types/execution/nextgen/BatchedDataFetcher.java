package com.intellij.lang.jsgraphql.types.execution.nextgen;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.DataFetcher;

@Internal
public interface BatchedDataFetcher<T> extends DataFetcher<T> {
}
