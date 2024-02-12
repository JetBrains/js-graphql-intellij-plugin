package com.intellij.lang.jsgraphql.javascript.workspace

import com.intellij.lang.jsgraphql.ide.workspace.GraphQLSourceRootFileSetData
import com.intellij.platform.workspace.storage.EntityStorage
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileKind
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileSetRegistrar

class GraphQLNodeModulesIndexContributor : WorkspaceFileIndexContributor<GraphQLNodeModulesEntity> {
  override val entityClass: Class<GraphQLNodeModulesEntity>
    get() = GraphQLNodeModulesEntity::class.java

  override fun registerFileSets(entity: GraphQLNodeModulesEntity, registrar: WorkspaceFileSetRegistrar, storage: EntityStorage) {
    entity.roots.forEach {
      registrar.registerFileSet(it, WorkspaceFileKind.EXTERNAL_SOURCE, entity, GraphQLSourceRootFileSetData())
    }
  }
}

