package com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters;

import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.ExecutionContext;
import com.intellij.lang.jsgraphql.types.execution.ExecutionStrategyParameters;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.Instrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.schema.DataFetchingEnvironment;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;

/**
 * Parameters sent to {@link Instrumentation} methods
 */
@PublicApi
public class InstrumentationFieldFetchParameters extends InstrumentationFieldParameters {
    private final DataFetchingEnvironment environment;
    private final ExecutionStrategyParameters executionStrategyParameters;
    private final boolean trivialDataFetcher;

    public InstrumentationFieldFetchParameters(ExecutionContext getExecutionContext, GraphQLFieldDefinition fieldDef, DataFetchingEnvironment environment, ExecutionStrategyParameters executionStrategyParameters, boolean trivialDataFetcher) {
        super(getExecutionContext, environment::getExecutionStepInfo);
        this.environment = environment;
        this.executionStrategyParameters = executionStrategyParameters;
        this.trivialDataFetcher = trivialDataFetcher;
    }

    private InstrumentationFieldFetchParameters(ExecutionContext getExecutionContext, DataFetchingEnvironment environment, InstrumentationState instrumentationState, ExecutionStrategyParameters executionStrategyParameters, boolean trivialDataFetcher) {
        super(getExecutionContext, environment::getExecutionStepInfo, instrumentationState);
        this.environment = environment;
        this.executionStrategyParameters = executionStrategyParameters;
        this.trivialDataFetcher = trivialDataFetcher;
    }

    /**
     * Returns a cloned parameters object with the new state
     *
     * @param instrumentationState the new state for this parameters object
     *
     * @return a new parameters object with the new state
     */
    @Override
    public InstrumentationFieldFetchParameters withNewState(InstrumentationState instrumentationState) {
        return new InstrumentationFieldFetchParameters(
                this.getExecutionContext(), this.getEnvironment(),
                instrumentationState, executionStrategyParameters, trivialDataFetcher);
    }


    public DataFetchingEnvironment getEnvironment() {
        return environment;
    }

    public boolean isTrivialDataFetcher() {
        return trivialDataFetcher;
    }
}
