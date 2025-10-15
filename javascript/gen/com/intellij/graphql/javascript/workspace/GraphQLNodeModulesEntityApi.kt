package com.intellij.graphql.javascript.workspace

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.ModifiableWorkspaceEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
interface ModifiableGraphQLNodeModulesEntity : ModifiableWorkspaceEntity<GraphQLNodeModulesEntity> {
  override var entitySource: EntitySource
  var roots: MutableSet<VirtualFileUrl>
}

internal object GraphQLNodeModulesEntityType : EntityType<GraphQLNodeModulesEntity, ModifiableGraphQLNodeModulesEntity>() {
  override val entityClass: Class<GraphQLNodeModulesEntity> get() = GraphQLNodeModulesEntity::class.java
  operator fun invoke(
    roots: Set<VirtualFileUrl>,
    entitySource: EntitySource,
    init: (ModifiableGraphQLNodeModulesEntity.() -> Unit)? = null,
  ): ModifiableGraphQLNodeModulesEntity {
    val builder = builder()
    builder.roots = roots.toMutableWorkspaceSet()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyGraphQLNodeModulesEntity(
  entity: GraphQLNodeModulesEntity,
  modification: ModifiableGraphQLNodeModulesEntity.() -> Unit,
): GraphQLNodeModulesEntity = modifyEntity(ModifiableGraphQLNodeModulesEntity::class.java, entity, modification)

@JvmOverloads
@JvmName("createGraphQLNodeModulesEntity")
fun GraphQLNodeModulesEntity(
  roots: Set<VirtualFileUrl>,
  entitySource: EntitySource,
  init: (ModifiableGraphQLNodeModulesEntity.() -> Unit)? = null,
): ModifiableGraphQLNodeModulesEntity = GraphQLNodeModulesEntityType(roots, entitySource, init)
