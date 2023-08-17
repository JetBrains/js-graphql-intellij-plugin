package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiModificationTracker

open class GraphQLConfigScope(
  project: Project,
  baseScope: GlobalSearchScope,
  protected val projectConfig: GraphQLProjectConfig,
) : DelegatingGlobalSearchScope(baseScope, projectConfig) {

  private val psiManager = PsiManager.getInstance(project)
  private val configProvider = GraphQLConfigProvider.getInstance(project)

  private val matchingFiles =
    GraphQLFileMatcherCache.newInstance(project, PsiModificationTracker.MODIFICATION_COUNT)

  final override fun contains(file: VirtualFile): Boolean {
    if (!super.contains(file)) {
      return false
    }

    return matchingFiles.value.match(file, ::match)
  }

  protected open fun match(file: VirtualFile): Boolean {
    val psiFile = psiManager.findFile(file) ?: return false
    if (projectConfig.isIncludedOutOfScopeFile(file)) {
      return true
    }

    val matchingConfig = configProvider.resolveProjectConfig(psiFile) ?: return false
    if (projectConfig == matchingConfig) {
      return true
    }
    // ensure that matched the same config file, even if it matched another project in that config, e.g. a fallback
    if (projectConfig.rootConfig != matchingConfig.rootConfig) {
      return false
    }
    // a resolved config could be just a first one matching or even a fallback,
    // so to support shared files between projects we need to match them manually
    return if (projectConfig.rootConfig.requiresSchemaStrictMatch(file)) {
      projectConfig.matchesSchema(psiFile)
    }
    else {
      projectConfig.matches(psiFile)
    }
  }
}
