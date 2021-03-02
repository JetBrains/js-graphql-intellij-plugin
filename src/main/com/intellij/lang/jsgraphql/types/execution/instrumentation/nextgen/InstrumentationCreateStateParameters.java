package com.intellij.lang.jsgraphql.types.execution.instrumentation.nextgen;

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

/**
 * Parameters sent to {@link com.intellij.lang.jsgraphql.types.execution.instrumentation.nextgen.Instrumentation} methods
 */
@Internal
public class InstrumentationCreateStateParameters {
    private final GraphQLSchema schema;
    private final ExecutionInput executionInput;

    public InstrumentationCreateStateParameters(GraphQLSchema schema, ExecutionInput executionInput) {
        this.schema = schema;
        this.executionInput = executionInput;
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    public ExecutionInput getExecutionInput() {
        return executionInput;
    }
}
