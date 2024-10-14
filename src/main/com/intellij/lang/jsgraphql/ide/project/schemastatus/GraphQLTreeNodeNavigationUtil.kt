/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.schemastatus

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.types.language.SourceLocation
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.StandardFileSystems

object GraphQLTreeNodeNavigationUtil {
  @JvmStatic
  fun openSourceLocation(myProject: Project, location: SourceLocation, followGeneratedFile: Boolean) {
    var sourceFile = StandardFileSystems.local().findFileByPath(location.sourceName) ?: return
    if (sourceFile.fileType == JsonFileType.INSTANCE && followGeneratedFile) {
      val generatedSource = GraphQLGeneratedSourcesManager.getInstance(myProject).requestGeneratedFile(sourceFile)
      if (generatedSource != null) {
        // open the SDL file and not the JSON introspection file it was based on
        sourceFile = generatedSource
      }
    }
    OpenFileDescriptor(myProject, sourceFile, location.line, location.column).navigate(true)
  }
}
