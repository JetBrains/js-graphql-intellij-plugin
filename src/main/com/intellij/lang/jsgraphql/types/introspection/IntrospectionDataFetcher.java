package com.intellij.lang.jsgraphql.types.introspection;

import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.TrivialDataFetcher;

/**
 * Special DataFetcher which is only used inside {@link Introspection}
 */
@Internal
public interface IntrospectionDataFetcher<T> extends TrivialDataFetcher<T> {
}
