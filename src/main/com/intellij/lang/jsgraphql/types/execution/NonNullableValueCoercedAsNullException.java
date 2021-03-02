package com.intellij.lang.jsgraphql.types.execution;

import com.intellij.lang.jsgraphql.types.ErrorType;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.PublicApi;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.language.VariableDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLInputObjectField;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLTypeUtil;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

/**
 * This is thrown if a non nullable value is coerced to a null value
 */
@PublicApi
public class NonNullableValueCoercedAsNullException extends GraphQLException implements GraphQLError {
    private List<SourceLocation> sourceLocations;

    public NonNullableValueCoercedAsNullException(VariableDefinition variableDefinition, GraphQLType graphQLType) {
        super(format("Variable '%s' has coerced Null value for NonNull type '%s'",
                variableDefinition.getName(), GraphQLTypeUtil.simplePrint(graphQLType)));
        this.sourceLocations = Collections.singletonList(variableDefinition.getSourceLocation());
    }

    public NonNullableValueCoercedAsNullException(VariableDefinition variableDefinition, String fieldName, GraphQLType graphQLType) {
        super(format("Field '%s' of variable '%s' has coerced Null value for NonNull type '%s'",
                fieldName, variableDefinition.getName(), GraphQLTypeUtil.simplePrint(graphQLType)));
        this.sourceLocations = Collections.singletonList(variableDefinition.getSourceLocation());
    }

    public NonNullableValueCoercedAsNullException(GraphQLInputObjectField inputTypeField) {
        super(format("Input field '%s' has coerced Null value for NonNull type '%s'",
                inputTypeField.getName(), GraphQLTypeUtil.simplePrint(inputTypeField.getType())));
    }

    @Override
    public List<SourceLocation> getLocations() {
        return sourceLocations;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.ValidationError;
    }
}
