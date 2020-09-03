/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.graphqlconfig.javascript;

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider;
import com.intellij.lang.jsgraphql.GraphQLBundle;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.openapi.project.Project;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Register the .graphqlconfig JSON schema
 */
public class GraphQLConfigJsonSchemaProvider implements JsonSchemaProviderFactory {
    @NotNull
    @Override
    public List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        return Collections.singletonList(
            new EmbeddedJsonSchemaFileProvider(
                "graphql-config-schema.json",
                GraphQLBundle.message("graphql.config"),
                null,
                GraphQLConfigJsonSchemaProvider.class,
                "/schemas/",
                GraphQLConfigManager.GRAPHQLCONFIG
            )
        );
    }
}
