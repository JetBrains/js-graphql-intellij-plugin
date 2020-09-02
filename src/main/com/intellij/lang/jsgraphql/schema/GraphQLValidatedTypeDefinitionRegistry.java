/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import graphql.GraphQLException;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.util.List;

public class GraphQLValidatedTypeDefinitionRegistry {

    private final TypeDefinitionRegistry registry;
    private final List<GraphQLException> errors;
    private final boolean processedGraphQL;

    public GraphQLValidatedTypeDefinitionRegistry(TypeDefinitionRegistry registry, List<GraphQLException> errors, boolean processedGraphQL) {
        this.registry = registry;
        this.errors = errors;
        this.processedGraphQL = processedGraphQL;
    }

    public TypeDefinitionRegistry getRegistry() {
        return registry;
    }

    public List<GraphQLException> getErrors() {
        return errors;
    }

    public boolean isProcessedGraphQL() {
        return processedGraphQL;
    }
}
