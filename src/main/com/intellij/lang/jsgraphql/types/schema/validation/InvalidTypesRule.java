package com.intellij.lang.jsgraphql.types.schema.validation;

import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.schema.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.types.schema.GraphQLUnknownType;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.GraphQLType;

public class InvalidTypesRule implements SchemaValidationRule {
    @Override
    public void check(GraphQLType type,
                      SchemaValidationErrorCollector validationErrorCollector) {
        if (type instanceof GraphQLUnknownType) {
            GraphQLError error = ((GraphQLUnknownType) type).getError();
            if (error != null) {
                validationErrorCollector.addError(new SchemaValidationError(error));
            }
        }
    }

    @Override
    public void check(GraphQLFieldDefinition fieldDef,
                      SchemaValidationErrorCollector validationErrorCollector) {
    }

    @Override
    public void check(GraphQLSchema graphQLSchema,
                      SchemaValidationErrorCollector validationErrorCollector) {
    }
}
