package com.intellij.lang.jsgraphql.ide.resolve.scope

import com.intellij.lang.jsgraphql.ide.search.GraphQLFileTypesProvider
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope

class GraphQLRestrictedFileTypesScope(baseScope: GlobalSearchScope) : DelegatingGlobalSearchScope(baseScope) {
  private val typesProvider = GraphQLFileTypesProvider.getService()

  override fun contains(file: VirtualFile): Boolean {
    return super.contains(file) && typesProvider.isAcceptedFile(file)
  }
}
