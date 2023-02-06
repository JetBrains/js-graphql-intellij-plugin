package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope

class GraphQLConfigSchemaScope(
    private val project: Project,
    baseScope: GlobalSearchScope,
    private val config: GraphQLProjectConfig
) : DelegatingGlobalSearchScope(baseScope, config) {

    override fun contains(file: VirtualFile): Boolean {
        return super.contains(file) && config.matchesSchema(file)
    }
}
