@file:JvmName("GraphQLConfigUtil")

package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.asSafely
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironmentParser
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile

fun getPhysicalVirtualFile(file: PsiFile?): VirtualFile? {
    return GraphQLPsiUtil.getPhysicalVirtualFile(file)
}

fun isLegacyConfig(file: VirtualFile?): Boolean {
    return isLegacyConfig(file?.name)
}

fun isLegacyConfig(filename: String?): Boolean {
    return filename?.lowercase() in LEGACY_CONFIG_NAMES
}

fun expandVariables(
    project: Project,
    map: Map<String, Any?>,
    dir: VirtualFile,
    isLegacy: Boolean,
    isUIContext: Boolean,
): Map<String, Any?> =
    map.mapValues { (_, value) ->
        when (value) {
            is String -> expandVariables(project, value, dir, isLegacy, isUIContext)
            is Map<*, *> -> expandVariables(project, parseMap(value) ?: emptyMap(), dir, isLegacy, isUIContext)
            else -> value
        }
    }

fun expandVariables(project: Project, raw: String, dir: VirtualFile, isLegacy: Boolean, isUIContext: Boolean): String {
    val environment = GraphQLConfigEnvironment.getInstance(project)
    return GraphQLConfigEnvironmentParser.getInstance(project).interpolate(raw, isLegacy) {
        environment.getVariable(it, dir, isUIContext)
    }.trim()
}

fun parseMap(value: Any?): Map<String, Any?>? =
    value.asSafely<Map<*, *>>()?.mapNotNull {
        val key = it.key as? String ?: return@mapNotNull null
        key to it.value
    }?.toMap()
