/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLFileMappingManager
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.types.InvalidSyntaxError
import com.intellij.lang.jsgraphql.types.language.SourceLocation
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.containers.ContainerUtil
import java.util.concurrent.ConcurrentMap

@Service(Service.Level.PROJECT)
class GraphQLRegistryProvider(project: Project) {

    companion object {
        private val LOG = logger<GraphQLRegistryProvider>()

        @JvmStatic
        fun getInstance(project: Project) = project.service<GraphQLRegistryProvider>()
    }

    private val psiManager = PsiManager.getInstance(project)
    private val configProvider = GraphQLConfigProvider.getInstance(project)
    private val scopeProvider = GraphQLScopeProvider.getInstance(project)
    private val fileMappingManager = GraphQLFileMappingManager.getInstance(project)
    private val psiSearchHelper = GraphQLPsiSearchHelper.getInstance(project)

    private val graphQLFilesScope = GlobalSearchScope.getScopeRestrictedByFileTypes(
        GlobalSearchScope.allScope(project),
        GraphQLFileType.INSTANCE,
    )
    private val jsonIntrospectionScope = GlobalSearchScope
        .getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JsonFileType.INSTANCE)

    private val scopeToRegistryCache: CachedValue<ConcurrentMap<GlobalSearchScope, GraphQLRegistryInfo>> =
        CachedValuesManager.getManager(project).createCachedValue {
            CachedValueProvider.Result.create(
                ContainerUtil.createConcurrentSoftMap(),
                GraphQLSchemaUtil.getSchemaDependencies(project),
            )
        }

    /**
     * @param context pass null for a global scope
     * @return registry for provided scope
     */
    fun getRegistryInfo(context: PsiElement?): GraphQLRegistryInfo {
        return getRegistryInfo(scopeProvider.getResolveScope(context, true))
    }

    fun getRegistryInfo(schemaScope: GlobalSearchScope): GraphQLRegistryInfo {
        return scopeToRegistryCache.value.computeIfAbsent(schemaScope) {
            val errors: MutableList<GraphQLException> = mutableListOf()
            val processor = GraphQLSchemaDocumentProcessor()

            // GraphQL files
            FileTypeIndex.processFiles(GraphQLFileType.INSTANCE, {
                val psiFile = psiManager.findFile(it)
                if (psiFile != null) {
                    processor.process(psiFile)
                }
                true
            }, graphQLFilesScope.intersectWith(schemaScope))

            // JSON GraphQL introspection result files
            if (configProvider.hasConfigurationFiles) {
                // need one or more configurations to be able to point "schemaPath" to relevant JSON files
                // otherwise all JSON files would be in scope
                FileTypeIndex.processFiles(
                    JsonFileType.INSTANCE,
                    { processJsonFile(processor, it, errors) },
                    jsonIntrospectionScope.intersectWith(schemaScope)
                )
            }

            // Injected GraphQL
            psiSearchHelper.processInjectedGraphQLPsiFiles(schemaScope, processor)

            val registry = processor.compositeRegistry.buildTypeDefinitionRegistry()
            GraphQLRegistryInfo(registry, errors, processor.isProcessed)
        }
    }

    private fun processJsonFile(
        processor: GraphQLSchemaDocumentProcessor,
        file: VirtualFile,
        errors: MutableList<GraphQLException>
    ): Boolean {
        // only JSON files that are directly referenced as "schemaPath" from the configuration will be
        // considered within scope, so we can just go ahead and try to turn the JSON into GraphQL
        val psiFile = psiManager.findFile(file) ?: return true
        try {
            processor.process(fileMappingManager.getOrCreateIntrospectionSDL(file))
        } catch (e: ProcessCanceledException) {
            throw e
        } catch (e: SchemaProblem) {
            errors.add(e)
        } catch (e: Exception) {
            val sourceLocation = listOf(
                SourceLocation(1, 1, GraphQLPsiUtil.getFileName(psiFile))
            )
            errors.add(SchemaProblem(listOf(InvalidSyntaxError(sourceLocation, e.message))))
        }
        return true
    }
}

