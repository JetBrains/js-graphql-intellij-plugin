package com.intellij.graphql.javascript.workspace

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

internal object GraphQLNodeModulesEntitySource : EntitySource

interface GraphQLNodeModulesEntity : WorkspaceEntity {
  val roots: Set<VirtualFileUrl>
}
