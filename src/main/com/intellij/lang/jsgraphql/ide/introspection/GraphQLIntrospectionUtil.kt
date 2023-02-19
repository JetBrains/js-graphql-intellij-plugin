@file:JvmName("GraphQLIntrospectionUtil")

package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.icons.AllIcons
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil


fun isJsonSchemaCandidate(document: Document?) =
    document?.text?.contains("__schema") == true

fun createIntrospectionLineMarker(
    project: Project,
    endpointNameCandidate: String,
    virtualFile: VirtualFile,
    projectName: String?,
    element: PsiElement,
): LineMarkerInfo<*>? {
    val endpointName = endpointNameCandidate.trim().takeIf { it.isNotBlank() } ?: return null
    val configProvider = GraphQLConfigProvider.getInstance(project)
    if (configProvider.isCachedConfigOutdated(virtualFile)) {
        return null
    }
    val config = configProvider.getForConfigFile(virtualFile) ?: return null
    val endpoint = config.findProject(projectName)?.endpoints?.find { it.name == endpointName } ?: return null

    val anchor = PsiTreeUtil.getDeepestFirst(element)
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
