package com.intellij.lang.jsgraphql.ide.search

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.findUsages.GraphQLFindUsagesUtil
import com.intellij.lang.jsgraphql.ide.search.scope.GraphQLMetaInfSchemaSearchScope
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryRootsProvider
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

@Service
class GraphQLScopeProvider(private val project: Project) {

    companion object {
        private val RESOLVE_SCOPE_KEY = Key.create<CachedValue<GlobalSearchScope>>("graphql.resolve.scope")

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLScopeProvider>()
    }

    private val globalScopeCached = CachedValuesManager.getManager(project).createCachedValue {
        CachedValueProvider.Result.create(
            createScope(null),
            GraphQLConfigProvider.getInstance(project),
            VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
        )
    }

    val globalScope
        get() = globalScopeCached.value

    fun getResolveScope(element: PsiElement?): GlobalSearchScope {
        if (element == null) {
            return globalScope
        }

        val file = element.containingFile
        return CachedValuesManager.getCachedValue(file, RESOLVE_SCOPE_KEY) {
            val configProvider = GraphQLConfigProvider.getInstance(project)
            val projectConfig = configProvider.resolveProjectConfig(file)
            val scope = projectConfig?.let { createScope(it.scope) }
                ?: globalScope.takeUnless { configProvider.hasAnyConfigFiles }
                ?: GlobalSearchScope.fileScope(file)

            CachedValueProvider.Result.create(
                scope,
                file,
                configProvider,
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
            )
        }
    }

    private fun createScope(configScope: GlobalSearchScope?): GlobalSearchScope {
        var scope = configScope ?: GlobalSearchScope.projectScope(project)

        // these scopes are used unconditionally, both for global and config filtered scopes
        scope = scope
            .union(createExternalDefinitionsLibraryScope())
            .union(GraphQLMetaInfSchemaSearchScope(project))

        // filter all the resulting scopes by file types, we don't want some child scope to override this
        val fileTypes = GraphQLFindUsagesUtil.getService().includedFileTypes.toTypedArray()
        return GlobalSearchScope.getScopeRestrictedByFileTypes(scope, *fileTypes)
    }

    private fun createExternalDefinitionsLibraryScope(): GlobalSearchScope {
        val roots = GraphQLLibraryRootsProvider.getLibraries(project)
            .asSequence()
            .flatMap { it.sourceRoots }
            .toSet()

        return GlobalSearchScope.filesWithLibrariesScope(project, roots)
    }
}
