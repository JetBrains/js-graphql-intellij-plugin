package com.intellij.lang.jsgraphql.ide.introspection.indexing

import com.intellij.lang.jsgraphql.ide.introspection.remote.GraphQLRemoteSchemasRegistry
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.indexing.IndexableSetContributor

class GraphQLIntrospectionIndexableSetContributor : IndexableSetContributor() {
  override fun getAdditionalRootsToIndex(): Set<VirtualFile> {
    return listOf(GraphQLGeneratedSourcesManager.generatedSdlDirPath, GraphQLRemoteSchemasRegistry.remoteSchemasDirPath)
      .mapNotNull { LocalFileSystem.getInstance().findFileByPath(it) }
      .filter { it.isValid }
      .toSet()
  }
}
