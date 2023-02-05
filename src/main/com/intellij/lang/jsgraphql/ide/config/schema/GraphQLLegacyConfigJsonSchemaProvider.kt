/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config.schema

import com.intellij.lang.javascript.EmbeddedJsonSchemaFileProvider
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.GRAPHQLCONFIG
import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

/**
 * Register the .graphqlconfig JSON schema
 */
class GraphQLLegacyConfigJsonSchemaProvider : JsonSchemaProviderFactory {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf<JsonSchemaFileProvider>(
            EmbeddedJsonSchemaFileProvider(
                "graphql-config-schema.json",
                GraphQLBundle.message("graphql.config.schema.name"),
                null,
                GraphQLLegacyConfigJsonSchemaProvider::class.java,
                "/schemas/",
                GRAPHQLCONFIG
            )
        )
    }
}
