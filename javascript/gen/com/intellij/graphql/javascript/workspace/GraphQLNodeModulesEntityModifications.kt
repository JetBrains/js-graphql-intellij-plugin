@file:JvmName("GraphQLNodeModulesEntityModifications")

package com.intellij.graphql.javascript.workspace

import com.intellij.platform.workspace.storage.EntitySource
import com.intellij.platform.workspace.storage.EntityType
import com.intellij.platform.workspace.storage.GeneratedCodeApiVersion
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntityBuilder
import com.intellij.platform.workspace.storage.impl.containers.toMutableWorkspaceSet
import com.intellij.platform.workspace.storage.url.VirtualFileUrl

@GeneratedCodeApiVersion(3)
interface GraphQLNodeModulesEntityBuilder : WorkspaceEntityBuilder<GraphQLNodeModulesEntity> {
  override var entitySource: EntitySource
  var roots: MutableSet<VirtualFileUrl>
}

internal object GraphQLNodeModulesEntityType : EntityType<GraphQLNodeModulesEntity, GraphQLNodeModulesEntityBuilder>() {
  override val entityClass: Class<GraphQLNodeModulesEntity> get() = GraphQLNodeModulesEntity::class.java
  operator fun invoke(
    roots: Set<VirtualFileUrl>,
    entitySource: EntitySource,
    init: (GraphQLNodeModulesEntityBuilder.() -> Unit)? = null,
  ): GraphQLNodeModulesEntityBuilder {
    val builder = builder()
    builder.roots = roots.toMutableWorkspaceSet()
    builder.entitySource = entitySource
    init?.invoke(builder)
    return builder
  }
}

fun MutableEntityStorage.modifyGraphQLNodeModulesEntity(
  entity: GraphQLNodeModulesEntity,
  modification: GraphQLNodeModulesEntityBuilder.() -> Unit,
): GraphQLNodeModulesEntity = modifyEntity(GraphQLNodeModulesEntityBuilder::class.java, entity, modification)

@JvmOverloads
@JvmName("createGraphQLNodeModulesEntity")
fun GraphQLNodeModulesEntity(
  roots: Set<VirtualFileUrl>,
  entitySource: EntitySource,
  init: (GraphQLNodeModulesEntityBuilder.() -> Unit)? = null,
): GraphQLNodeModulesEntityBuilder = GraphQLNodeModulesEntityType(roots, entitySource, init)
