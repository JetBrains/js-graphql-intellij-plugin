package com.intellij.lang.jsgraphql.ide.introspection.editor.yaml

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.lang.jsgraphql.ide.config.MODERN_CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.introspection.createIntrospectionLineMarker
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.*

class GraphQLIntrospectionYamlLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val keyValue = element as? YAMLKeyValue ?: return null
        val file = keyValue.containingFile as? YAMLFile ?: return null
        val virtualFile = file.virtualFile ?: return null
        if (virtualFile.name !in MODERN_CONFIG_NAMES) {
            return null
        }

        val endpointsKeyValue = getParentKeyValueWithName(keyValue, GraphQLConfigKeys.EXTENSION_ENDPOINTS) ?: return null
        val extensionsKeyValue = getParentKeyValueWithName(endpointsKeyValue, GraphQLConfigKeys.EXTENSIONS) ?: return null

        val isRoot = isRootKeyValue(extensionsKeyValue)
        val projectName = if (!isRoot) {
            getParentKeyValue(extensionsKeyValue)?.keyText
        } else {
            null
        }
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
            createIntrospectionLineMarker(element.project, keyValue.keyText, virtualFile, projectName, keyValue)
        else {
            null
        }
    }

    private fun getParentKeyValueWithName(element: YAMLPsiElement?, name: String): YAMLKeyValue? {
        return getParentKeyValue(element)?.takeIf { it.keyText == name }
    }

    private fun getParentKeyValue(element: YAMLPsiElement?): YAMLKeyValue? {
        var parent = element?.parent
        if (parent is YAMLMapping) {
            parent = parent.parent
        }
        return parent as? YAMLKeyValue
    }

    private fun isRootKeyValue(element: YAMLPsiElement?): Boolean {
        return element?.parent
            ?.takeIf { it is YAMLMapping }
            ?.parent
            ?.let { it is YAMLDocument }
            ?: false
    }
}
