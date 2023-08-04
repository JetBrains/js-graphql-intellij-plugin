package com.intellij.lang.jsgraphql.ide.introspection.editor.yaml

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.lang.jsgraphql.ide.config.MODERN_CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.introspection.createIntrospectionLineMarker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.io.URLUtil
import org.jetbrains.yaml.psi.*

class GraphQLIntrospectionYamlLineMarkerProvider : LineMarkerProvider {
  override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
    val file = element.containingFile as? YAMLFile ?: return null
    val virtualFile = file.virtualFile ?: return null
    if (virtualFile.name !in MODERN_CONFIG_NAMES) {
      return null
    }

    return when (element) {
      is YAMLSequenceItem, is YAMLScalar -> createRemoteSchemaMarker(element, virtualFile)
      is YAMLKeyValue -> createEndpointMarker(element, virtualFile)
      else -> null
    }
  }

  private fun createRemoteSchemaMarker(
    element: PsiElement,
    virtualFile: VirtualFile,
  ): LineMarkerInfo<*>? {
    val schemaKeyValue = getParentKeyValueWithName(element, GraphQLConfigKeys.SCHEMA) ?: return null
    val isRoot = isRootKeyValue(schemaKeyValue)
    val projectName: String? = if (!isRoot) findProjectName(schemaKeyValue) else null
    if (!isRoot && projectName.isNullOrBlank()) {
      return null
    }

    return when (element) {
      // schema:
      //   - https://url.com:
      //       headers:
      //         Authorization: ${TOKEN}
      is YAMLSequenceItem -> {
        when (val value = element.value) {
          is YAMLMapping -> createForSchemaObject(value.keyValues.firstOrNull(), virtualFile, projectName)
          else -> null
        }
      }

      // schema: https://url.com
      // or
      // schema:
      //   - https://url.com
      is YAMLScalar -> createForSchemaScalar(element, virtualFile, projectName)

      else -> null
    }
  }

  private fun findProjectName(element: PsiElement): String? {
    val projectNameCandidate = getParentKeyValue(element)
    val projectsKeyValue = getParentKeyValueWithName(projectNameCandidate, GraphQLConfigKeys.PROJECTS)
    return if (isRootKeyValue(projectsKeyValue)) {
      projectNameCandidate?.keyText
    }
    else {
      null
    }
  }

  private fun createForSchemaObject(
    keyValue: YAMLKeyValue?,
    virtualFile: VirtualFile,
    projectName: String?,
  ): LineMarkerInfo<*>? {
    return keyValue?.keyText
      ?.takeIf { URLUtil.canContainUrl(it) }
      ?.let {
        createIntrospectionLineMarker(keyValue.project, it, virtualFile, projectName, keyValue)
      }
  }

  private fun createForSchemaScalar(
    scalar: YAMLScalar,
    virtualFile: VirtualFile,
    projectName: String?,
  ): LineMarkerInfo<*>? {
    val textValue = scalar.textValue
    return if (textValue.startsWith("\${") || URLUtil.canContainUrl(textValue)) {
      createIntrospectionLineMarker(scalar.project, textValue, virtualFile, projectName, scalar)
    }
    else null
  }

  private fun createEndpointMarker(keyValue: YAMLKeyValue, virtualFile: VirtualFile): LineMarkerInfo<*>? {
    val endpointsKeyValue =
      getParentKeyValueWithName(keyValue, GraphQLConfigKeys.EXTENSION_ENDPOINTS) ?: return null
    val extensionsKeyValue =
      getParentKeyValueWithName(endpointsKeyValue, GraphQLConfigKeys.EXTENSIONS) ?: return null

    val isRoot = isRootKeyValue(extensionsKeyValue)
    val projectName = if (!isRoot) findProjectName(extensionsKeyValue) else null
    if (!isRoot && projectName.isNullOrBlank()) {
      return null
    }

    val isValidEndpoint = when (val value = keyValue.value) {
      is YAMLScalar -> true

      is YAMLMapping -> value
        .getKeyValueByKey(GraphQLConfigKeys.EXTENSION_ENDPOINT_URL)
        ?.takeIf { it.value is YAMLScalar } != null

      else -> false
    }

    return if (isValidEndpoint)
      createIntrospectionLineMarker(keyValue.project, keyValue.keyText, virtualFile, projectName, keyValue)
    else {
      null
    }
  }

  private fun getParentKeyValueWithName(element: PsiElement?, name: String): YAMLKeyValue? {
    return getParentKeyValue(element)?.takeIf { it.keyText == name }
  }

  private fun getParentKeyValue(element: PsiElement?): YAMLKeyValue? {
    return PsiTreeUtil.skipParentsOfType(
      element,
      YAMLMapping::class.java,
      YAMLSequenceItem::class.java,
      YAMLSequence::class.java
    ) as? YAMLKeyValue
  }

  private fun isRootKeyValue(element: PsiElement?): Boolean {
    return element?.parent
      ?.takeIf { it is YAMLMapping }
      ?.parent is YAMLDocument
  }
}
