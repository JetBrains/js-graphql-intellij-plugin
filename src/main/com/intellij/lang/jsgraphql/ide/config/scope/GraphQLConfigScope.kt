package com.intellij.lang.jsgraphql.ide.config.scope

import com.intellij.lang.jsgraphql.ide.config.GraphQLProjectConfig
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.DelegatingGlobalSearchScope
import com.intellij.psi.search.GlobalSearchScope

class GraphQLConfigScope(baseScope: GlobalSearchScope, private val config: GraphQLProjectConfig) :
    DelegatingGlobalSearchScope(baseScope, config) {

    override fun contains(file: VirtualFile): Boolean {
        return super.contains(file) && config.match(file)
    }

}
