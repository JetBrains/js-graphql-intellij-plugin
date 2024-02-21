@file:JvmName("GraphQLIndexUtil")

package com.intellij.lang.jsgraphql.ide.indexing

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.openapi.fileTypes.FileType

const val INDEX_BASE_VERSION = 3

@JvmField
val FILE_TYPES_WITH_IGNORED_SIZE_LIMIT: Collection<FileType> = setOf(GraphQLFileType.INSTANCE, JsonFileType.INSTANCE)
