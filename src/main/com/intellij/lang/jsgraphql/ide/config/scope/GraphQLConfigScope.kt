package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiModificationTracker

class GraphQLConfigScope(
    project: Project,
    baseScope: GlobalSearchScope,
    private val config: GraphQLProjectConfig
) : DelegatingGlobalSearchScope(baseScope, config) {

    private val configProvider = GraphQLConfigProvider.getInstance(project)
    private val matchingFiles =
        GraphQLFileMatcherCache.newInstance(project, PsiModificationTracker.MODIFICATION_COUNT)

    override fun contains(file: VirtualFile): Boolean {
        if (!super.contains(file)) {
            return false
        }

        return matchingFiles.value.match(file) {
            // The matching logic considers both glob patterns and specific corner cases,
            // such as utilizing the first project with empty `include` and `exclude` arrays
            // in the absence of an exact match.
            configProvider.resolveProjectConfig(file) == config
        }
    }
}
