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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.util.io.URLUtil

class GraphQLIntrospectionJsonLineMarkerProvider : LineMarkerProvider {
  override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
    val file = element.containingFile as? JsonFile ?: return null
    val virtualFile = file.virtualFile ?: return null
    if (virtualFile.name !in CONFIG_NAMES || virtualFile.name == "package.json") return null

    val schemaProperty = getParentPropertyWithName(element, GraphQLConfigKeys.SCHEMA)
    return if (schemaProperty != null) {
      createSchemaMarker(element, schemaProperty, virtualFile)
    }
    else {
      createEndpointMarker(element, virtualFile)
    }
  }

  private fun createSchemaMarker(
    element: PsiElement?,
    schemaProperty: JsonProperty,
    virtualFile: VirtualFile,
  ): LineMarkerInfo<*>? {
    val isRoot = isRootProperty(schemaProperty)
    val projectName = if (!isRoot) findProjectName(schemaProperty) else null
    if (!isRoot && projectName.isNullOrBlank()) {
      return null
    }

    return when (element) {
      is JsonObject -> createForSchemaObject(element.propertyList.firstOrNull(), virtualFile, projectName)
      is JsonStringLiteral -> createForSchemaString(element, virtualFile, projectName)
      else -> null
    }
  }

  private fun createForSchemaObject(
    property: JsonProperty?,
    virtualFile: VirtualFile,
    projectName: String?,
  ): LineMarkerInfo<*>? {
    return property?.name
      ?.takeIf { URLUtil.canContainUrl(it) }
      ?.let {
        createIntrospectionLineMarker(property.project, it, virtualFile, projectName, property)
      }
  }

  private fun createForSchemaString(
    element: JsonStringLiteral,
    virtualFile: VirtualFile,
    projectName: String?,
  ): LineMarkerInfo<*>? {
    val textValue = element.value
    return if (textValue.startsWith("\${") || URLUtil.canContainUrl(textValue)) {
      createIntrospectionLineMarker(element.project, textValue, virtualFile, projectName, element)
    }
    else null
  }

  private fun createEndpointMarker(element: PsiElement?, virtualFile: VirtualFile): LineMarkerInfo<*>? {
    val property = element as? JsonProperty ?: return null
    val endpointsProperty =
      getParentPropertyWithName(property, GraphQLConfigKeys.EXTENSION_ENDPOINTS) ?: return null
    val extensionsProperty =
      getParentPropertyWithName(endpointsProperty, GraphQLConfigKeys.EXTENSIONS) ?: return null

    val isRoot = isRootProperty(extensionsProperty)
    val projectName = if (!isRoot) findProjectName(extensionsProperty) else null
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

  private fun findProjectName(extensionsProperty: JsonProperty): String? {
    val projectNameCandidate = getParentProperty(extensionsProperty)
    val projects = getParentPropertyWithName(projectNameCandidate, GraphQLConfigKeys.PROJECTS)
    return if (isRootProperty(projects)) projectNameCandidate?.name else null
  }

  private fun getParentPropertyWithName(element: PsiElement?, name: String): JsonProperty? {
    return getParentProperty(element)?.takeIf { it.name == name }
  }

  private fun getParentProperty(element: PsiElement?): JsonProperty? {
    var parent = element?.parent
    if (parent is JsonObject || parent is JsonArray) {
      parent = parent.parent
    }
    return parent as? JsonProperty
  }

  private fun isRootProperty(element: PsiElement?): Boolean {
    return element?.parent
      ?.takeIf { it is JsonObject }
      ?.parent is JsonFile
  }
}
