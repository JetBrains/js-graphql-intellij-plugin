package com.intellij.lang.jsgraphql.javascript.workspace

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

  //region generated code
  @GeneratedCodeApiVersion(3)
  interface Builder : WorkspaceEntity.Builder<GraphQLNodeModulesEntity> {
    override var entitySource: EntitySource
    var roots: MutableSet<VirtualFileUrl>
  }

  companion object : EntityType<GraphQLNodeModulesEntity, Builder>() {
    @JvmOverloads
    @JvmStatic
    @JvmName("create")
    operator fun invoke(
      roots: Set<VirtualFileUrl>,
      entitySource: EntitySource,
      init: (Builder.() -> Unit)? = null,
    ): Builder {
      val builder = builder()
      builder.roots = roots.toMutableWorkspaceSet()
      builder.entitySource = entitySource
      init?.invoke(builder)
      return builder
    }
  }
  //endregion
}

//region generated code
fun MutableEntityStorage.modifyGraphQLNodeModulesEntity(
  entity: GraphQLNodeModulesEntity,
  modification: GraphQLNodeModulesEntity.Builder.() -> Unit,
): GraphQLNodeModulesEntity {
  return modifyEntity(GraphQLNodeModulesEntity.Builder::class.java, entity, modification)
}
//endregion
