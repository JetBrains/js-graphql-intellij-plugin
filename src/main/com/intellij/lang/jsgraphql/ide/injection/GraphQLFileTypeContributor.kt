/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileTypes.FileType

/**
 * Contributor that enables the plugin to locate GraphQL in additional file types such as JavaScript
 */
interface GraphQLFileTypeContributor {
  companion object {
    @JvmField
    val EP_NAME: ExtensionPointName<GraphQLFileTypeContributor> = ExtensionPointName("com.intellij.lang.jsgraphql.fileTypeContributor")

    @JvmStatic
    fun getAllFileTypes(): Collection<FileType> = EP_NAME.extensionList.flatMapTo(LinkedHashSet()) { it.getFileTypes() }
  }

  /**
   * A list of additional file types that the plugin should process to locate GraphQL in a project
   */
  fun getFileTypes(): Collection<FileType>
}
