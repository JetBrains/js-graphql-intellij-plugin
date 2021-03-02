package com.intellij.lang.jsgraphql.types.execution.instrumentation;

import com.intellij.lang.jsgraphql.types.PublicSpi;

/**
 * An {@link Instrumentation} implementation can create this as a stateful object that is then passed
 * to each instrumentation method, allowing state to be passed down with the request execution
 *
 * @see Instrumentation#createState(com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationCreateStateParameters)
 */
@PublicSpi
public interface InstrumentationState {
}
