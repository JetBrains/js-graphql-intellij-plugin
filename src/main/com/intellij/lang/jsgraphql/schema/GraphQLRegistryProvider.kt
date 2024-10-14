/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.annotations.ApiStatus

@Deprecated("Use GraphQLSchemaProvider instead")
@ApiStatus.ScheduledForRemoval
@Service(Service.Level.PROJECT)
class GraphQLRegistryProvider(private val project: Project) {

  companion object {
    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLRegistryProvider>()
  }

  @Deprecated("Use GraphQLSchemaProvider instead")
  @ApiStatus.ScheduledForRemoval
  fun getRegistryInfo(context: PsiElement?): GraphQLRegistryInfo {
    return getRegistryInfo(GraphQLScopeProvider.getInstance(project).getResolveScope(context, true))
  }

  @Deprecated("Use GraphQLSchemaProvider instead")
  @ApiStatus.ScheduledForRemoval
  fun getRegistryInfo(schemaScope: GlobalSearchScope): GraphQLRegistryInfo {
    val schemaInfo = GraphQLSchemaProvider.getInstance(project).getSchemaInfo(schemaScope)
    return GraphQLRegistryInfo(schemaInfo.registry, schemaInfo.isTooComplex)
  }
}

