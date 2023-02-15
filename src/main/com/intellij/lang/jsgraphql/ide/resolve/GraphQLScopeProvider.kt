package com.intellij.lang.jsgraphql.ide.resolve

import com.intellij.ide.scratch.ScratchUtil
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourceManager
import com.intellij.lang.jsgraphql.ide.resolve.scope.GraphQLMetaInfSchemaSearchScope
import com.intellij.lang.jsgraphql.ide.resolve.scope.GraphQLRestrictedFileTypesScope
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentSpread
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier
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

@Service(Service.Level.PROJECT)
class GraphQLScopeProvider(private val project: Project) {

    companion object {
        private val NON_STRICT_SCOPE_KEY =
            Key.create<CachedValue<GlobalSearchScope>>("graphql.non.strict.scope")
        private val STRICT_SCOPE_KEY =
            Key.create<CachedValue<GlobalSearchScope>>("graphql.strict.scope")

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLScopeProvider>()

        @JvmStatic
        fun createScope(project: Project, customScope: GlobalSearchScope?): GlobalSearchScope {
            var scope = customScope ?: GlobalSearchScope.projectScope(project)

            // these scopes are used unconditionally, both for global and config filtered scopes
            scope = scope
                .union(createExternalDefinitionsLibraryScope(project))
                .union(GraphQLMetaInfSchemaSearchScope(project))

            return GraphQLRestrictedFileTypesScope(scope)
        }

        private fun createExternalDefinitionsLibraryScope(project: Project): GlobalSearchScope {
            val roots = GraphQLLibraryRootsProvider.getLibraries(project)
                .asSequence()
                .flatMap { it.sourceRoots }
                .toSet()

            return GlobalSearchScope.filesWithLibrariesScope(project, roots)
        }

        fun isResolvedInNonStrictScope(element: PsiElement?): Boolean {
            val context = if (element is GraphQLIdentifier) element.parent else element
            return context is GraphQLFragmentSpread
        }
    }

    private val configProvider = GraphQLConfigProvider.getInstance(project)
    private val generatedSourceManager = GraphQLGeneratedSourceManager.getInstance(project)

    private val globalScopeCache: CachedValue<GlobalSearchScope> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                createScope(project, null),
                GraphQLConfigProvider.getInstance(project),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
            )
        }

    val globalScope: GlobalSearchScope
        get() = globalScopeCache.value

    @RequiresReadLock
    fun getResolveScope(element: PsiElement?): GlobalSearchScope {
        return getResolveScope(element, !isResolvedInNonStrictScope(element))
    }

    @RequiresReadLock
    fun getResolveScope(element: PsiElement?, isStrict: Boolean): GlobalSearchScope {
        if (element == null) {
            return GlobalSearchScope.EMPTY_SCOPE
        }

        return if (isStrict)
            getOrCreateScope(element, STRICT_SCOPE_KEY)
        else
            getOrCreateScope(element, NON_STRICT_SCOPE_KEY)
    }

    private fun getOrCreateScope(
        element: PsiElement,
        key: Key<CachedValue<GlobalSearchScope>>
    ): GlobalSearchScope {
        val file = element.containingFile
        return CachedValuesManager.getCachedValue(file, key) {
            val projectConfig = configProvider.resolveConfig(file)
            var scope: GlobalSearchScope =
                projectConfig?.let { if (key == STRICT_SCOPE_KEY) it.schemaScope else it.scope }
                    ?: globalScope.takeUnless { configProvider.hasConfigurationFiles }
                    ?: createScope(project, GlobalSearchScope.fileScope(file))

            if (ScratchUtil.isScratch(file.virtualFile)) {
                scope = scope.union(GlobalSearchScope.fileScope(file))
            }

            CachedValueProvider.Result.create(
                scope,
                file,
                configProvider,
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                generatedSourceManager,
            )
        }
    }
}
