package com.intellij.lang.jsgraphql.types.execution.nextgen;

import com.intellij.lang.jsgraphql.types.*;
import com.intellij.lang.jsgraphql.types.execution.Async;
import com.intellij.lang.jsgraphql.types.execution.ExecutionId;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;

import java.util.concurrent.CompletableFuture;

@Internal
public class Execution {

    ExecutionHelper executionHelper = new ExecutionHelper();

    public CompletableFuture<ExecutionResult> execute(ExecutionStrategy executionStrategy,
                                                      Document document,
                                                      GraphQLSchema graphQLSchema,
                                                      ExecutionId executionId,
                                                      ExecutionInput executionInput,
                                                      InstrumentationState instrumentationState) {
        ExecutionHelper.ExecutionData executionData;
        try {
            executionData = executionHelper.createExecutionData(document, graphQLSchema, executionId, executionInput, instrumentationState);
        } catch (RuntimeException rte) {
            if (rte instanceof GraphQLError) {
                return CompletableFuture.completedFuture(new ExecutionResultImpl((GraphQLError) rte));
            }
            return Async.exceptionallyCompletedFuture(rte);
        }

        try {
            return executionStrategy
                    .execute(executionData.executionContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
