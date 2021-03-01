package com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;

/**
 * Parameters sent to {@link Instrumentation} methods
 */
@SuppressWarnings("TypeParameterUnusedInFormals")
@PublicApi
public class InstrumentationExecuteOperationParameters {
    private final ExecutionContext executionContext;
    private final InstrumentationState instrumentationState;

    public InstrumentationExecuteOperationParameters(ExecutionContext executionContext) {
        this(executionContext, executionContext.getInstrumentationState());
    }

    private InstrumentationExecuteOperationParameters(ExecutionContext executionContext, InstrumentationState instrumentationState) {
        this.executionContext = executionContext;
        this.instrumentationState = instrumentationState;
    }

    /**
     * Returns a cloned parameters object with the new state
     *
     * @param instrumentationState the new state for this parameters object
     *
     * @return a new parameters object with the new state
     */
    public InstrumentationExecuteOperationParameters withNewState(InstrumentationState instrumentationState) {
        return new InstrumentationExecuteOperationParameters(executionContext, instrumentationState);
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public <T extends InstrumentationState> T getInstrumentationState() {
        //noinspection unchecked
        return (T) instrumentationState;
    }
}
