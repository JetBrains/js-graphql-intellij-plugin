/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.config.model

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.expandVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GraphQLConfigEndpoint(
    private val project: Project,
    private val data: GraphQLRawEndpoint,
    val dir: VirtualFile,
    private val configPointer: GraphQLConfigPointer?,
    val isLegacy: Boolean,
    val isUIContext: Boolean,
) {
    val name: String = data.name
        ?: url
        ?: configPointer?.file?.name
        ?: dir.path

    val url: String?
        get() = data.url?.let { expandVariables(project, it, dir, isLegacy, isUIContext) }

    val headers: Map<String, Any?>
        get() = expandVariables(project, data.headers, dir, isLegacy, isUIContext)

    val file: VirtualFile?
        get() = configPointer?.file

    fun copyWithUIContext(newContext: Boolean): GraphQLConfigEndpoint =
        if (newContext == isUIContext) this else GraphQLConfigEndpoint(project, data, dir, configPointer, isLegacy, newContext)

    fun findConfig(): GraphQLProjectConfig? {
        val provider = GraphQLConfigProvider.getInstance(project)
        val config = provider.getConfig(configPointer?.file)
        return config?.findProject(configPointer?.projectName) ?: config?.getDefault()
    }
}

/**
 * Endpoint model is a long-lived object inside the editor,
 * so we avoid hard references to the GraphQLProjectConfig.
 */
data class GraphQLConfigPointer(val file: VirtualFile?, val projectName: String?)
