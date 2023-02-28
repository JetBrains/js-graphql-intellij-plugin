/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GraphQLRegistryInfo {

    private final TypeDefinitionRegistry myRegistry;
    private final List<GraphQLException> myErrors;
    private final boolean myHasProcessedAnyFile;

    public GraphQLRegistryInfo(@NotNull TypeDefinitionRegistry registry,
                               @NotNull List<GraphQLException> errors,
                               boolean processedAnyFile) {
        myRegistry = registry;
        myErrors = errors;
        myHasProcessedAnyFile = processedAnyFile;
    }

    public @NotNull TypeDefinitionRegistry getTypeDefinitionRegistry() {
        return myRegistry;
    }

    public @NotNull List<GraphQLException> getErrors() {
        List<GraphQLException> errors = new ArrayList<>();
        errors.addAll(myRegistry.getErrors());
        errors.addAll(myErrors);
        return errors;
    }

    public boolean hasProcessedAnyFile() {
        return myHasProcessedAnyFile;
    }
}
