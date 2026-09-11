package com.intellij.lang.jsgraphql.ide.resolve

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile
import com.intellij.util.concurrency.annotations.RequiresReadLock

interface GraphQLGlobalResolveFilter {
  companion object {
    @JvmStatic
    val EP_NAME: ExtensionPointName<GraphQLGlobalResolveFilter> =
      ExtensionPointName.create("com.intellij.lang.jsgraphql.globalResolveFilter")

    @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
    fun isGlobalResolveForcedFor(context: PsiFile): Boolean {
      return EP_NAME.extensionList.any { it.isGlobalResolveFor(context) }
    }
  }

  @RequiresReadLock(generateAssertion = false /* IJPL-115548 */)
  fun isGlobalResolveFor(file: PsiFile): Boolean
}