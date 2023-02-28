/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.lang.jsgraphql.ide.validation.GraphQLErrorFilter;
import com.intellij.lang.jsgraphql.types.GraphQLError;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GraphQLSchemaInfo {

    private final GraphQLSchema mySchema;
    private final List<GraphQLException> myErrors;
    private final GraphQLRegistryInfo myRegistry;

    public GraphQLSchemaInfo(@NotNull GraphQLSchema schema,
                             @NotNull List<GraphQLException> errors,
                             @NotNull GraphQLRegistryInfo registry) {
        mySchema = schema;
        myErrors = errors;
        myRegistry = registry;
    }

    public @NotNull GraphQLSchema getSchema() {
        return mySchema;
    }

    public @NotNull GraphQLRegistryInfo getRegistryInfo() {
        return myRegistry;
    }

    public @NotNull List<GraphQLError> getErrors(@NotNull Project project) {
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
                errors.addAll(((InvalidSchemaException) exception).getErrors());
            } else {
                errors.add(new GraphQLUnexpectedSchemaError(exception));
            }
        }

        return ContainerUtil.filter(errors, error ->
            GraphQLErrorFilter.EP_NAME.extensions().noneMatch(filter -> filter.isGraphQLErrorSuppressed(project, error, null))
        );
    }
}
