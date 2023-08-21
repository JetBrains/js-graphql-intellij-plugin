/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config.jsonSchema

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

private val MODERN_YML_JSON_CONFIGS = arrayOf(
  GRAPHQL_CONFIG_JSON,
  GRAPHQL_CONFIG_YAML,
  GRAPHQL_CONFIG_YML,
  GRAPHQL_RC,
  GRAPHQL_RC_JSON,
  GRAPHQL_RC_YAML,
  GRAPHQL_RC_YML,
)

class GraphQLConfigJsonSchemaProvider : JsonSchemaProviderFactory {

  override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
    return listOf<JsonSchemaFileProvider>(
      GraphQLEmbeddedJsonSchemaFileProvider(
        "graphql-config-schema.json",
        GraphQLBundle.message("graphql.config.legacy.schema.name"),
        null,
        GraphQLConfigJsonSchemaProvider::class.java,
        "/schemas/",
        GRAPHQLCONFIG,
      ),
      GraphQLEmbeddedJsonSchemaFileProvider(
        "graphql-config.json",
        GraphQLBundle.message("graphql.config.schema.name"),
        null,
        GraphQLConfigJsonSchemaProvider::class.java,
        "/schemas/",
        *MODERN_YML_JSON_CONFIGS,
      )
    )
  }

  private class GraphQLEmbeddedJsonSchemaFileProvider(
    @NlsSafe private val resourceName: String,
    @NlsContexts.ListItem private val presentableName: String?,
    private val remoteSourceUrl: String?,
    clazz: Class<*>,
    pathToFilename: String,
    vararg files: String,
  ) : JsonSchemaFileProvider {

    private val schemaFile: VirtualFile? = JsonSchemaProviderFactory.getResourceFile(clazz, pathToFilename + resourceName)
    private val userFilename: Set<String> = if (files.isEmpty()) setOf(resourceName) else files.toSet()

    override fun isAvailable(file: VirtualFile): Boolean {
      return userFilename.contains(file.name)
    }

    override fun getName(): String {
      return resourceName
    }

    override fun getSchemaFile(): VirtualFile? {
      return schemaFile
    }

    override fun getSchemaType(): SchemaType {
      return SchemaType.embeddedSchema
    }

    override fun getPresentableName(): String {
      return presentableName ?: name
    }

    override fun isUserVisible(): Boolean {
      return presentableName != null
    }

    override fun getRemoteSource(): String? {
      return remoteSourceUrl
    }
  }

}
