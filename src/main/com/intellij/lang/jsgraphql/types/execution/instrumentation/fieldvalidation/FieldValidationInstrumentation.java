package com.intellij.lang.jsgraphql.types.execution.instrumentation.fieldvalidation;

import com.intellij.lang.jsgraphql.types.ExecutionResult;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.execution.AbortExecutionException;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.InstrumentationContext;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.SimpleInstrumentation;
import com.intellij.lang.jsgraphql.types.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;

import java.util.List;

import static com.intellij.lang.jsgraphql.types.Assert.assertNotNull;

/**
 * This {@link graphql.execution.instrumentation.Instrumentation} allows you to validate the fields
 * of the query before the query is executed.  You need to provide an implementation of
 * {@link FieldValidation} that is called to validate fields.  If it returns errors
 * then the query execution will be aborted and the errors will be returned
 * in the execution result
 *
 * @see FieldValidation
 */
@PublicApi
public class FieldValidationInstrumentation extends SimpleInstrumentation {

    private final FieldValidation fieldValidation;

    /**
     * Your field validation will be called before query execution
     *
     * @param fieldValidation the field validation to call
     */
    public FieldValidationInstrumentation(FieldValidation fieldValidation) {
        this.fieldValidation = assertNotNull(fieldValidation);
    }

    @Override
    public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {

        List<GraphQLError> errors = FieldValidationSupport.validateFieldsAndArguments(fieldValidation, parameters.getExecutionContext());
        if (errors != null && !errors.isEmpty()) {
            throw new AbortExecutionException(errors);
        }
        return super.beginExecuteOperation(parameters);
    }
}
