package com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStepInfo;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

import java.util.function.Supplier;

/**
 * Parameters sent to {@link Instrumentation} methods
 */
@PublicApi
public class InstrumentationFieldParameters {
    private final ExecutionContext executionContext;
    private final Supplier<ExecutionStepInfo> executionStepInfo;
    private final InstrumentationState instrumentationState;

    public InstrumentationFieldParameters(ExecutionContext executionContext, Supplier<ExecutionStepInfo> executionStepInfo) {
        this(executionContext, executionStepInfo, executionContext.getInstrumentationState());
    }

    InstrumentationFieldParameters(ExecutionContext executionContext, Supplier<ExecutionStepInfo> executionStepInfo, InstrumentationState instrumentationState) {
        this.executionContext = executionContext;
        this.executionStepInfo = executionStepInfo;
        this.instrumentationState = instrumentationState;
    }

    /**
     * Returns a cloned parameters object with the new state
     *
     * @param instrumentationState the new state for this parameters object
     * @return a new parameters object with the new state
     */
    public InstrumentationFieldParameters withNewState(InstrumentationState instrumentationState) {
        return new InstrumentationFieldParameters(
                this.executionContext, this.executionStepInfo, instrumentationState);
    }


    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public GraphQLFieldDefinition getField() {
        return executionStepInfo.get().getFieldDefinition();
    }

    public ExecutionStepInfo getExecutionStepInfo() {
        return executionStepInfo.get();
    }

    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T extends InstrumentationState> T getInstrumentationState() {
        //noinspection unchecked
        return (T) instrumentationState;
    }
}
