package com.intellij.lang.jsgraphql.schema.library

import com.intellij.platform.workspace.storage.*
import com.intellij.platform.workspace.storage.annotations.Default
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet
import com.intellij.platform.workspace.storage.url.VirtualFileUrl
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls

@GeneratedCodeApiVersion(3)
interface ModifiableGraphQLLibraryEntity : ModifiableWorkspaceEntity<GraphQLLibraryEntity> {
  override var entitySource: EntitySource
  var identifier: String
  var displayName: String
  var description: String?
  var attachmentScope: GraphQLLibraryAttachmentScope
  var roots: MutableSet<VirtualFileUrl>
}

internal object GraphQLLibraryEntityType : EntityType<GraphQLLibraryEntity, ModifiableGraphQLLibraryEntity>() {
  override val entityClass: Class<GraphQLLibraryEntity> get() = GraphQLLibraryEntity::class.java
  operator fun invoke(
    identifier: String,
    displayName: String,
    roots: Set<VirtualFileUrl>,
    entitySource: EntitySource,
    init: (ModifiableGraphQLLibraryEntity.() -> Unit)? = null,
  ): ModifiableGraphQLLibraryEntity {
    val builder = builder()
    builder.identifier = identifier
    builder.displayName = displayName
    builder.roots = roots.toMutableWorkspaceSet()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyGraphQLLibraryEntity(
  entity: GraphQLLibraryEntity,
  modification: ModifiableGraphQLLibraryEntity.() -> Unit,
): GraphQLLibraryEntity = modifyEntity(ModifiableGraphQLLibraryEntity::class.java, entity, modification)

@JvmOverloads
@JvmName("createGraphQLLibraryEntity")
fun GraphQLLibraryEntity(
  identifier: String,
  displayName: String,
  roots: Set<VirtualFileUrl>,
  entitySource: EntitySource,
  init: (ModifiableGraphQLLibraryEntity.() -> Unit)? = null,
): ModifiableGraphQLLibraryEntity = GraphQLLibraryEntityType(identifier, displayName, roots, entitySource, init)
