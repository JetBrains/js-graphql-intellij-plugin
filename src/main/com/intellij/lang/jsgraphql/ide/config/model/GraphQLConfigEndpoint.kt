/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEnvironmentSnapshot
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.env.extractEnvironmentVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.URLUtil

// NOTE: update hashCode and equals when changed
class GraphQLConfigEndpoint(
  private val project: Project,
  val rawData: GraphQLRawEndpoint,
  val dir: VirtualFile,
  val isLegacy: Boolean,
  val environment: GraphQLEnvironmentSnapshot,
  private val rawSchemaPointer: GraphQLRawSchemaPointer?,
  val config: GraphQLProjectConfig?,
) {
  val key: String? = rawData.name ?: rawData.url // raw url is used intentionally

  val url: String? = expandVariables(rawData.url, createExpandContext())

  val displayName: String = rawData.name
                            ?: url
                            ?: config?.file?.name
                            ?: dir.path

  val headers: Map<String, Any?> = parseMap(expandVariables(rawData.headers, createExpandContext())) ?: emptyMap()

  val file: VirtualFile? = config?.file

  val projectName: String? = config?.name

  val introspect: Boolean? = rawData.introspect

  val isValidUrl: Boolean = url?.let { URLUtil.canContainUrl(url) } ?: false

  val schemaPointer: GraphQLSchemaPointer? = rawSchemaPointer?.let { GraphQLSchemaPointer(project, dir, it, isLegacy, environment) }

  val usedVariables: Collection<String> = extractEnvironmentVariables(project, isLegacy, rawData.url, rawData.headers)

  fun withUpdatedEnvironment(): GraphQLConfigEndpoint =
    GraphQLConfigEndpoint(project, rawData, dir, isLegacy, environment.update(project, dir), rawSchemaPointer, config)

  private fun createExpandContext() = GraphQLExpandVariableContext(project, dir, isLegacy, environment)

  override fun toString(): String {
    val endpointName = displayName
    val endpointUrl = url

    return if (endpointUrl == null) {
      endpointName
    }
    else if (endpointName == endpointUrl) {
      endpointUrl
    }
    else {
      "$displayName - $url"
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GraphQLConfigEndpoint

    if (project != other.project) return false
    if (rawData != other.rawData) return false
    if (dir != other.dir) return false
    if (isLegacy != other.isLegacy) return false
    if (environment != other.environment) return false
    return rawSchemaPointer == other.rawSchemaPointer
  }

  override fun hashCode(): Int {
    var result = project.hashCode()
    result = 31 * result + rawData.hashCode()
    result = 31 * result + dir.hashCode()
    result = 31 * result + isLegacy.hashCode()
    result = 31 * result + environment.hashCode()
    result = 31 * result + (rawSchemaPointer?.hashCode() ?: 0)
    return result
  }
}
