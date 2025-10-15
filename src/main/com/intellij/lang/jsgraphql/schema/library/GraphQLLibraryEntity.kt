package com.intellij.lang.jsgraphql.schema.library

import com.intellij.platform.workspace.storage.*
import com.intellij.platform.workspace.storage.annotations.Default
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

internal object GraphQLLibraryEntitySource : EntitySource

interface GraphQLLibraryEntity : WorkspaceEntity {
  @get:NonNls
  val identifier: String

  @get:Nls
  val displayName: String

  @get:Nls
  val description: String?

  val attachmentScope: GraphQLLibraryAttachmentScope
    @Default get() = GraphQLLibraryAttachmentScope.GLOBAL

  val roots: Set<VirtualFileUrl>
}
