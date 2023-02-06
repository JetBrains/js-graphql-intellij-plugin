package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope

class GraphQLConfigScope(
    project: Project,
    baseScope: GlobalSearchScope,
    private val config: GraphQLProjectConfig
) : DelegatingGlobalSearchScope(baseScope, config) {

    private val configProvider = GraphQLConfigProvider.getInstance(project)

    override fun contains(file: VirtualFile): Boolean {
        // The matching logic considers both glob patterns and specific corner cases,
        // such as utilizing the first project with empty `include` and `exclude` arrays
        // in the absence of an exact match.
        return super.contains(file) && configProvider.resolveProjectConfig(file) === config
    }

}
