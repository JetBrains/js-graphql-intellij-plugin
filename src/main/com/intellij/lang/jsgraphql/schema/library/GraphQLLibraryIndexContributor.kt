package com.intellij.lang.jsgraphql.schema.library

import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileKind
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileSetRegistrar

class GraphQLLibraryIndexContributor : WorkspaceFileIndexContributor<GraphQLLibraryEntity> {
  override val entityClass: Class<GraphQLLibraryEntity>
    get() = GraphQLLibraryEntity::class.java

  override fun registerFileSets(entity: GraphQLLibraryEntity, registrar: WorkspaceFileSetRegistrar, storage: EntityStorage) {
    entity.roots.forEach {
      registrar.registerFileSet(it, WorkspaceFileKind.EXTERNAL_SOURCE, entity, GraphQLLibrarySourceRootData())
    }
  }
}