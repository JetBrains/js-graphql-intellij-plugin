/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import graphql.GraphQLError;
import graphql.GraphQLException;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.errors.SchemaProblem;

import java.util.List;

public class GraphQLValidatedSchema {

    private final GraphQLSchema schema;
    private final List<GraphQLException> exceptions;
    private final GraphQLValidatedTypeDefinitionRegistry registry;

    public GraphQLValidatedSchema(GraphQLSchema schema, List<GraphQLException> errors, GraphQLValidatedTypeDefinitionRegistry registry) {
        this.schema = schema;
        this.exceptions = errors;
        this.registry = registry;
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    public List<GraphQLException> getExceptions() {
        return exceptions;
    }

    public GraphQLValidatedTypeDefinitionRegistry getRegistry() {
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
            } else {
                errors.add(new GraphQLInternalSchemaError(exception));
            }
        }
        return errors;
    }
}
