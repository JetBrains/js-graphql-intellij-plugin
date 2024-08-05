/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.javascript.injection

import com.intellij.lang.javascript.DialectDetector
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil
import com.intellij.lang.jsgraphql.ide.injection.GraphQLFileTypeContributor
import com.intellij.openapi.fileTypes.FileType

/**
 * Registers the various JavaScript languages to support GraphQL in tagged templates
 */
private class GraphQLJavaScriptFileTypeContributor : GraphQLFileTypeContributor {
  override fun getFileTypes(): Collection<FileType> {
    val fileTypeSet = mutableSetOf<FileType>()
    fileTypeSet.addAll(TypeScriptUtil.TYPESCRIPT_FILE_TYPES)
    fileTypeSet.addAll(DialectDetector.JAVASCRIPT_FILE_TYPES)
    return fileTypeSet
  }
}
