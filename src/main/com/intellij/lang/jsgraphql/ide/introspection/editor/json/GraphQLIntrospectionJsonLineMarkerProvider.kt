/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection.editor.json

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.json.psi.*
import com.intellij.lang.jsgraphql.ide.config.CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.introspection.createIntrospectionLineMarker
import com.intellij.psi.PsiElement

class GraphQLIntrospectionJsonLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val property = element as? JsonProperty ?: return null
        val file = element.containingFile as? JsonFile ?: return null
        val virtualFile = file.virtualFile ?: return null
        if (virtualFile.name !in CONFIG_NAMES || virtualFile.name == "package.json") return null

        val endpointsProperty = getParentPropertyWithName(property, GraphQLConfigKeys.EXTENSION_ENDPOINTS) ?: return null
        val extensionsProperty = getParentPropertyWithName(endpointsProperty, GraphQLConfigKeys.EXTENSIONS) ?: return null

        val isRoot = isRootProperty(extensionsProperty)
        val projectName = if (!isRoot) {
            getParentProperty(extensionsProperty)?.name
        } else {
            null
        }
        if (!isRoot && projectName.isNullOrBlank()) {
            return null
        }

        val isValidEndpoint = when (val value = property.value) {
            is JsonStringLiteral -> true

            is JsonObject -> value
                .findProperty(GraphQLConfigKeys.EXTENSION_ENDPOINT_URL)
                ?.takeIf { it.value is JsonStringLiteral } != null

            else -> false
        }

        return if (isValidEndpoint)
            createIntrospectionLineMarker(property.project, property.name, virtualFile, projectName, property)
        else {
            null
        }
    }

    private fun getParentPropertyWithName(element: JsonElement?, name: String): JsonProperty? {
        return getParentProperty(element)?.takeIf { it.name == name }
    }

    private fun getParentProperty(element: JsonElement?): JsonProperty? {
        var parent = element?.parent
        if (parent is JsonObject) {
            parent = parent.parent
        }
        return parent as? JsonProperty
    }

    private fun isRootProperty(element: JsonElement?): Boolean {
        return element?.parent
            ?.takeIf { it is JsonObject }
            ?.parent
            ?.let { it is JsonFile }
            ?: false
    }
}
