package com.intellij.lang.jsgraphql.ide.introspection.editor.yaml

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.MODERN_CONFIG_NAMES
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLConfigKeys
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.*

class GraphQLIntrospectionYamlLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val project = element.project
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
        if (!isValidEndpoint) {
            return null
        }

        val endpointName = keyValue.keyText.trim().takeIf { it.isNotBlank() } ?: return null
        val configProvider = GraphQLConfigProvider.getInstance(project)
        if (configProvider.isCachedConfigOutdated(virtualFile)) {
            return null
        }
        val config = configProvider.getForConfigFile(virtualFile) ?: return null
        val endpoint = config.findProject(projectName)?.endpoints?.find { it.name == endpointName } ?: return null

        val anchor = PsiTreeUtil.getDeepestFirst(keyValue)
        return LineMarkerInfo(
            anchor,
            anchor.textRange,
            AllIcons.RunConfigurations.TestState.Run,
            { GraphQLBundle.message("graphql.introspection.run.query") },
            { _, _ ->
                GraphQLIntrospectionService.getInstance(project).performIntrospectionQueryAndUpdateSchemaPathFile(endpoint)
            },
            GutterIconRenderer.Alignment.CENTER,
            GraphQLBundle.messagePointer("graphql.introspection.run.query")
        )
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
