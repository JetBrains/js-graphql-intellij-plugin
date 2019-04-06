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
            } else {
                errors.add(new GraphQLInternalSchemaError(exception));
            }
        }
        return errors;
    }
}
