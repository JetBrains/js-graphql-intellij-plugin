/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import graphql.AssertException;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.language.SourceLocation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.errors.SchemaProblem;

import java.util.Collections;
import java.util.List;

public class GraphQLSchemaWithErrors {

    private final GraphQLSchema schema;
    private final List<GraphQLException> exceptions;
    private final TypeDefinitionRegistryWithErrors registry;

    public GraphQLSchemaWithErrors(GraphQLSchema schema, List<GraphQLException> errros, TypeDefinitionRegistryWithErrors registry) {
        this.schema = schema;
        this.exceptions = errros;
        this.registry = registry;
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    public List<GraphQLException> getExceptions() {
        return exceptions;
    }

    public TypeDefinitionRegistryWithErrors getRegistry() {
        return registry;
    }

    public boolean isErrorsPresent() {
        return !exceptions.isEmpty() || !registry.getErrors().isEmpty();
    }

    public List<GraphQLError> getErrors() {
        final List<GraphQLException> rawErrors = Lists.newArrayList(exceptions);
        rawErrors.addAll(registry.getErrors());
        final List<GraphQLError> errors = Lists.newArrayList();
        for (GraphQLException exception : rawErrors) {
            if (exception instanceof SchemaProblem) {
                errors.addAll(((SchemaProblem) exception).getErrors());
            } else if (exception instanceof GraphQLError) {
                errors.add((GraphQLError) exception);
            } else if (exception instanceof AssertException) {
                errors.add(new GraphQLError() {
                    @Override
                    public String getMessage() {
                        // strip out graphql-java internals part of the exception message given that the schema can actually be broken/invalid as the editor changes
                        return exception.getMessage().replace("Internal error: should never happen: ", "");
                    }

                    @Override
                    public List<SourceLocation> getLocations() {
                        return Collections.emptyList();
                    }

                    @Override
                    public ErrorType getErrorType() {
                        return ErrorType.ValidationError;
                    }
                });
            }
        }
        return errors;
    }
}
