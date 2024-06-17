/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.UniqueVFilePathBuilder
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile

class GraphQLIntrospectionEditorTabTitleProvider : EditorTabTitleProvider {
  override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
    val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
    if (generatedSourcesManager.isGeneratedFile(file)) {
      return generatedSourcesManager.getSourceFile(file)
        ?.let { runReadAction { UniqueVFilePathBuilder.getInstance().getUniqueVirtualFilePath(project, it) } }
        ?.let { GraphQLBundle.message("graphql.tab.title.graphql.schema", it) }
    }
    val schemasRegistry = GraphQLRemoteSchemasRegistry.getInstance(project)
    if (schemasRegistry.isRemoteSchemaFile(file)) {
      val config = GraphQLConfigProvider.getInstance(project)
                     .getForConfigFile(schemasRegistry.getSourceFile(file)) ?: return null

      return config.getProjects().values
        .asSequence()
        .flatMap { it.schema }
        .find { it.isRemote && FileUtil.pathsEqual(it.outputPath, file.path) }
        ?.let { GraphQLBundle.message("graphql.tab.title.graphql.schema", it.url) }
    }
    return null
  }
}
