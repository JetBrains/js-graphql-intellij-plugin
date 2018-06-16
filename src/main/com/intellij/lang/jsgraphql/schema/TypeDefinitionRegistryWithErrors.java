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

public class TypeDefinitionRegistryWithErrors {

    private final TypeDefinitionRegistry registry;
    private final List<GraphQLException> errors;

    public TypeDefinitionRegistryWithErrors(TypeDefinitionRegistry registry, List<GraphQLException> errors) {
        this.registry = registry;
        this.errors = errors;
    }

    public TypeDefinitionRegistry getRegistry() {
        return registry;
    }

    public List<GraphQLException> getErrors() {
        return errors;
    }
}
