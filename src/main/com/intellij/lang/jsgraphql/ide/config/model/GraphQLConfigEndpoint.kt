/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLEnvironmentSnapshot
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.parseMap
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class GraphQLConfigEndpoint(
    private val project: Project,
    private val data: GraphQLRawEndpoint,
    val dir: VirtualFile,
    private val configPointer: GraphQLConfigPointer?,
    val isLegacy: Boolean,
    val environment: GraphQLEnvironmentSnapshot,
) {
    val name: String? = data.name ?: data.url // raw url is used intentionally

    val url: String? = expandVariables(data.url, GraphQLExpandVariableContext(project, dir, isLegacy, environment))

    val displayName: String = data.name
        ?: url
        ?: configPointer?.file?.name
        ?: dir.path

    val headers: Map<String, Any?> = parseMap(
        expandVariables(
            data.headers,
            GraphQLExpandVariableContext(project, dir, isLegacy, environment)
        )
    ) ?: emptyMap()

    val file: VirtualFile? = configPointer?.file

    val projectName: String? = configPointer?.projectName

    val introspect: Boolean? = data.introspect

    fun withCurrentEnvironment(): GraphQLConfigEndpoint {
        return copy(environment = GraphQLConfigEnvironment.getInstance(project).createSnapshot(environment.variables.keys, dir))
    }

    fun findConfig(): GraphQLProjectConfig? {
        val provider = GraphQLConfigProvider.getInstance(project)
        val config = provider.getForConfigFile(configPointer?.fileOrDir)
        return config?.findProject(configPointer?.projectName) ?: config?.getDefault()
    }

    override fun toString(): String {
        val endpointName = displayName
        val endpointUrl = url

        return if (endpointUrl == null) {
            endpointName
        } else if (endpointName == endpointUrl) {
            endpointUrl
        } else {
            "$displayName - $url"
        }
    }
}

/**
 * Endpoint model is a long-lived object inside the editor,
 * so we avoid hard references to the GraphQLProjectConfig.
 */
data class GraphQLConfigPointer(val fileOrDir: VirtualFile?, val projectName: String?) {
    val file = fileOrDir?.takeUnless { it.isDirectory }
}
