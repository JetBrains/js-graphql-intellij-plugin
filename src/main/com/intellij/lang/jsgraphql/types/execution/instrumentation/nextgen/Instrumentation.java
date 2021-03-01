package com.intellij.lang.jsgraphql.types.execution.instrumentation.nextgen;

import com.intellij.lang.jsgraphql.types.ExecutionInput;
import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.Internal;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.DocumentAndVariables;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationState;
import com.intellij.lang.jsgraphql.types.language.Document;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.validation.ValidationError;

import java.util.List;

import static com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentationContext.noOp;

@Internal
public interface Instrumentation {

    default InstrumentationState createState(InstrumentationCreateStateParameters parameters) {
        return new InstrumentationState() {
        };
    }

    default ExecutionInput instrumentExecutionInput(ExecutionInput executionInput, InstrumentationExecutionParameters parameters) {
        return executionInput;
    }

    default DocumentAndVariables instrumentDocumentAndVariables(DocumentAndVariables documentAndVariables, InstrumentationExecutionParameters parameters) {
        return documentAndVariables;
    }

    default GraphQLSchema instrumentSchema(GraphQLSchema graphQLSchema, InstrumentationExecutionParameters parameters) {
        return graphQLSchema;
    }

    default ExecutionResult instrumentExecutionResult(ExecutionResult result, InstrumentationExecutionParameters parameters) {
        return result;
    }

    default InstrumentationContext<ExecutionResult> beginExecution(InstrumentationExecutionParameters parameters) {
        return noOp();
    }

    default InstrumentationContext<Document> beginParse(InstrumentationExecutionParameters parameters) {
        return noOp();
    }

    default InstrumentationContext<List<ValidationError>> beginValidation(InstrumentationValidationParameters parameters) {
        return noOp();
    }
}
