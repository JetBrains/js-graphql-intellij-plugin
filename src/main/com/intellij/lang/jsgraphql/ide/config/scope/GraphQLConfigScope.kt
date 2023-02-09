package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiModificationTracker

open class GraphQLConfigScope(
    project: Project,
    baseScope: GlobalSearchScope,
    protected val config: GraphQLProjectConfig
) : DelegatingGlobalSearchScope(baseScope, config) {

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
        // The matching logic considers both glob patterns and specific corner cases,
        // such as utilizing the first project with empty `include` and `exclude` arrays
        // in the absence of an exact match or using a default project if the whole config is empty.
        //
        // Scope checks will be evaluated against the whole project,
        // so we need to run a complete matching algorithm, including a search for the nearest config file.
        // Otherwise, it's possible to include a random file from non-related subdirectory, e.g. when we have an empty config.
        return configProvider.resolveProjectConfig(file) == config
    }
}
