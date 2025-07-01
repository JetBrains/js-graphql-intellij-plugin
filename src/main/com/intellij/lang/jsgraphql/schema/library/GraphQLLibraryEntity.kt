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

  //region generated code
  @GeneratedCodeApiVersion(3)
  interface Builder : WorkspaceEntity.Builder<GraphQLLibraryEntity> {
    override var entitySource: EntitySource
    var identifier: String
    var displayName: String
    var description: String?
    var attachmentScope: GraphQLLibraryAttachmentScope
    var roots: MutableSet<VirtualFileUrl>
  }

  companion object : EntityType<GraphQLLibraryEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(
      identifier: String,
      displayName: String,
      roots: Set<VirtualFileUrl>,
      entitySource: EntitySource,
      init: (Builder.() -> Unit)? = null,
    ): Builder {
      val builder = builder()
      builder.identifier = identifier
      builder.displayName = displayName
      builder.roots = roots.toMutableWorkspaceSet()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion
}

//region generated code
fun MutableEntityStorage.modifyGraphQLLibraryEntity(
  entity: GraphQLLibraryEntity,
  modification: GraphQLLibraryEntity.Builder.() -> Unit,
): GraphQLLibraryEntity {
  return modifyEntity(GraphQLLibraryEntity.Builder::class.java, entity, modification)
}
//endregion
