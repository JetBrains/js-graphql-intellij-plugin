package com.intellij.lang.jsgraphql.ide.project.model

import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.WritingAccessProvider

class GraphQLIntrospectionWritingAccessProvider(project: Project) : WritingAccessProvider() {

  private val generatedSourcesManager = GraphQLGeneratedSourcesManager.getInstance(project)
  private val remoteSchemasRegistry = GraphQLRemoteSchemasRegistry.getInstance(project)

  override fun requestWriting(files: Collection<VirtualFile>): Collection<VirtualFile> {
    return files.filter { generatedSourcesManager.isGeneratedFile(it) || remoteSchemasRegistry.isRemoteSchemaFile(it) }
  }

  override fun isPotentiallyWritable(file: VirtualFile): Boolean {
    return !generatedSourcesManager.isGeneratedFile(file) && !remoteSchemasRegistry.isRemoteSchemaFile(file)
  }
}
