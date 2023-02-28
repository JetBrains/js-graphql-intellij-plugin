@file:JvmName("GraphQLConfigUtil")

package com.intellij.lang.jsgraphql.ide.config

import com.intellij.lang.jsgraphql.asSafely
import com.intellij.openapi.vfs.VirtualFile

fun isLegacyConfig(file: VirtualFile?): Boolean {
    return isLegacyConfig(file?.name)
}

fun isLegacyConfig(filename: String?): Boolean {
    return filename?.lowercase() in LEGACY_CONFIG_NAMES
}

fun parseMap(value: Any?): Map<String, Any?>? =
    value.asSafely<Map<*, *>>()?.mapNotNull {
        val key = it.key as? String ?: return@mapNotNull null
        key to it.value
    }?.toMap()
