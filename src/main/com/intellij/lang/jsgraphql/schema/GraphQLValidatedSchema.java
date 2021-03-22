/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException;
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationError;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class GraphQLValidatedSchema {

    private final GraphQLSchema mySchema;
    private final List<GraphQLException> myErrors;
    private final GraphQLValidatedRegistry myRegistry;

    public GraphQLValidatedSchema(@NotNull GraphQLSchema schema,
                                  @NotNull List<GraphQLException> errors,
                                  @NotNull GraphQLValidatedRegistry registry) {
        mySchema = schema;
        myErrors = errors;
        myRegistry = registry;
    }

    public @NotNull GraphQLSchema getSchema() {
        return mySchema;
    }

    public @NotNull GraphQLValidatedRegistry getRegistry() {
        return myRegistry;
    }

    public boolean hasErrors() {
        return !myErrors.isEmpty() || !myRegistry.getErrors().isEmpty() || !mySchema.getErrors().isEmpty();
    }

    public @NotNull List<GraphQLError> getErrors() {
        final List<GraphQLException> rawErrors = Lists.newArrayList(myErrors);
        rawErrors.addAll(myRegistry.getErrors());
        rawErrors.addAll(mySchema.getErrors());

        final List<GraphQLError> errors = Lists.newArrayList();
        for (GraphQLException exception : rawErrors) {
            if (exception instanceof SchemaProblem) {
                errors.addAll(((SchemaProblem) exception).getErrors());
            } else if (exception instanceof GraphQLError) {
                errors.add((GraphQLError) exception);
            } else if (exception instanceof InvalidSchemaException) {
                Collection<SchemaValidationError> validationErrors = ((InvalidSchemaException) exception).getErrors();
                for (SchemaValidationError validationError : validationErrors) {
                    if (validationError.getBaseError() != null) {
                        errors.add(validationError.getBaseError());
                    }
                    // TODO: [intellij] handle other error types
                }
            } else {
                errors.add(new GraphQLUnexpectedSchemaError(exception));
            }
        }
        return errors;
    }
}
