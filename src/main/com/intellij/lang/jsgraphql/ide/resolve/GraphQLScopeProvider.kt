package com.intellij.lang.jsgraphql.ide.resolve

import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.findUsages.GraphQLFindUsagesUtil
import com.intellij.lang.jsgraphql.ide.resolve.scope.GraphQLMetaInfSchemaSearchScope
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
import com.intellij.util.concurrency.annotations.RequiresReadLock
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentMap

@Service
class GraphQLScopeProvider(private val project: Project) {

    companion object {
        private val RESOLVE_SCOPE_KEY = Key.create<CachedValue<GlobalSearchScope>>("graphql.resolve.scope")

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLScopeProvider>()

        @JvmStatic
        fun createScope(project: Project, customScope: GlobalSearchScope?): GlobalSearchScope {
            var scope = customScope ?: GlobalSearchScope.projectScope(project)

            // these scopes are used unconditionally, both for global and config filtered scopes
            scope = scope
                .union(createExternalDefinitionsLibraryScope(project))
                .union(GraphQLMetaInfSchemaSearchScope(project))

            // filter all the resulting scopes by file types, we don't want some child scope to override this
            val fileTypes = GraphQLFindUsagesUtil.getService().includedFileTypes.toTypedArray()
            return GlobalSearchScope.getScopeRestrictedByFileTypes(scope, *fileTypes)
        }

        private fun createExternalDefinitionsLibraryScope(project: Project): GlobalSearchScope {
            val roots = GraphQLLibraryRootsProvider.getLibraries(project)
                .asSequence()
                .flatMap { it.sourceRoots }
                .toSet()

            return GlobalSearchScope.filesWithLibrariesScope(project, roots)
        }
    }

    private val globalScopeCache: CachedValue<GlobalSearchScope> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                createScope(project, null),
                GraphQLConfigProvider.getInstance(project),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
        }

    private val scopeByConfigCache: CachedValue<ConcurrentMap<GraphQLProjectConfig, GlobalSearchScope>> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                ContainerUtil.createConcurrentWeakMap(),
                GraphQLConfigProvider.getInstance(project),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
        }

    val globalScope: GlobalSearchScope
        get() = globalScopeCache.value

    @RequiresReadLock
    fun getResolveScope(element: PsiElement?): GlobalSearchScope {
        if (element == null) {
            return GlobalSearchScope.EMPTY_SCOPE
        }

        val file = element.containingFile
        return CachedValuesManager.getCachedValue(file, RESOLVE_SCOPE_KEY) {
            val configProvider = GraphQLConfigProvider.getInstance(project)
            val projectConfig = configProvider.resolveProjectConfig(file)
            val scope = getConfigResolveScope(projectConfig)
                ?: globalScope.takeUnless { configProvider.hasConfigurationFiles }
                ?: createScope(project, GlobalSearchScope.fileScope(file))

            CachedValueProvider.Result.create(
                scope,
                file,
                configProvider,
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
            )
        }
    }

    @RequiresReadLock
    fun getConfigResolveScope(config: GraphQLProjectConfig?): GlobalSearchScope? {
        if (config == null) {
            return null
        }

        val cache = scopeByConfigCache.value
        val prev = cache[config]
        if (prev != null) {
            return prev
        }

        val scope = createScope(project, config.scope)
        return cache.putIfAbsent(config, scope) ?: scope
    }
}
