package com.intellij.lang.jsgraphql.ide.project.model

import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.GeneratedSourcesFilter
import com.intellij.openapi.vfs.VirtualFile

class GraphQLIntrospectionSourcesFilter : GeneratedSourcesFilter() {
  override fun isGeneratedSource(file: VirtualFile, project: Project): Boolean =
    GraphQLGeneratedSourcesManager.getInstance(project).isGeneratedFile(file) ||
    GraphQLRemoteSchemasRegistry.getInstance(project).isRemoteSchemaFile(file)
}
