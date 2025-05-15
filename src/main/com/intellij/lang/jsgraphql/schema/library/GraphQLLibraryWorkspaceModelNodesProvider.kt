package com.intellij.lang.jsgraphql.schema.library

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNode
import com.intellij.ide.projectView.impl.nodes.ExternalLibrariesWorkspaceModelNodesProvider
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.validOrNull
import com.intellij.platform.backend.workspace.virtualFile

class GraphQLLibraryWorkspaceModelNodesProvider : ExternalLibrariesWorkspaceModelNodesProvider<GraphQLLibraryEntity> {
  override fun getWorkspaceClass(): Class<GraphQLLibraryEntity> = GraphQLLibraryEntity::class.java

  override fun createNode(entity: GraphQLLibraryEntity, project: Project, settings: ViewSettings?): AbstractTreeNode<*>? =
    ExternalLibrariesWorkspaceModelNode(project,
                                        entity.roots.mapNotNull { it.virtualFile?.validOrNull() },
                                        GraphQLBundle.message("graphql.library.prefix", entity.displayName),
                                        settings)
}