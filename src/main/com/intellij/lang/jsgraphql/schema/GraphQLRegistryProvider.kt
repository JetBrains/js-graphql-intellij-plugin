/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentMap

@Service(Service.Level.PROJECT)
class GraphQLRegistryProvider(private val project: Project) {

  companion object {
    private val LOG = logger<GraphQLRegistryProvider>()

    @JvmStatic
    fun getInstance(project: Project) = project.service<GraphQLRegistryProvider>()
  }

  private val psiManager = PsiManager.getInstance(project)
  private val scopeProvider = GraphQLScopeProvider.getInstance(project)
  private val psiSearchHelper = GraphQLPsiSearchHelper.getInstance(project)

  private val scopeToRegistryCache: CachedValue<ConcurrentMap<GlobalSearchScope, GraphQLRegistryInfo>> =
    CachedValuesManager.getManager(project).createCachedValue {
      CachedValueProvider.Result.create(
        ContainerUtil.createConcurrentSoftMap(),
        GraphQLSchemaContentTracker.getInstance(project),
      )
    }

  /**
   * @param context pass null for a global scope
   * @return registry for provided scope
   */
  fun getRegistryInfo(context: PsiElement?): GraphQLRegistryInfo {
    return getRegistryInfo(scopeProvider.getResolveScope(context, true))
  }

  fun getRegistryInfo(schemaScope: GlobalSearchScope): GraphQLRegistryInfo {
    return scopeToRegistryCache.value.computeIfAbsent(schemaScope) {
      val processor = GraphQLSchemaDocumentProcessor()

      // GraphQL files
      FileTypeIndex.processFiles(
        GraphQLFileType.INSTANCE,
        {
          val psiFile = psiManager.findFile(it)
          if (psiFile != null) {
            processor.process(psiFile)
          }
          true
        },
        GlobalSearchScope.getScopeRestrictedByFileTypes(schemaScope, GraphQLFileType.INSTANCE)
      )

      // Injected GraphQL
      psiSearchHelper.processInjectedGraphQLFiles(project, schemaScope, processor)

      processor.compositeRegistry.build()
    }
  }
}

