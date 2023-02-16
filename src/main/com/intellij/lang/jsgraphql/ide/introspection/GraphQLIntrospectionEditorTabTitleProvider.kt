/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.introspection

import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.fileEditor.UniqueVFilePathBuilder
import com.intellij.openapi.fileEditor.impl.EditorTabTitleProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class GraphQLIntrospectionEditorTabTitleProvider : EditorTabTitleProvider {
    override fun getEditorTabTitle(project: Project, file: VirtualFile): String? {
        val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
        if (generatedSourcesManager.isGeneratedFile(file)) {
            return generatedSourcesManager.getSourceFile(file)
                ?.let { UniqueVFilePathBuilder.getInstance().getUniqueVirtualFilePath(project, it) }
                ?.let { "GraphQL Schema ($it)" }
        }
        return null
    }
}
