/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Maps;
import com.intellij.json.JsonFileType;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionFilesManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.lang.jsgraphql.types.GraphQLException;
import com.intellij.lang.jsgraphql.types.InvalidSyntaxError;
import com.intellij.lang.jsgraphql.types.language.SourceLocation;
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry;
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.TimeoutUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GraphQLRegistryProvider implements Disposable {

    private static final Logger LOG = Logger.getInstance(GraphQLRegistryProvider.class);

    private final GraphQLPsiSearchHelper graphQLPsiSearchHelper;
    private final Project myProject;
    private final GlobalSearchScope graphQLFilesScope;
    private final GlobalSearchScope jsonIntrospectionScope;
    private final PsiManager psiManager;
    private final GraphQLConfigManager graphQLConfigManager;

    private final Map<GlobalSearchScope, GraphQLRegistryInfo> scopeToRegistry = Maps.newConcurrentMap();

    public static GraphQLRegistryProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLRegistryProvider.class);
    }

    public GraphQLRegistryProvider(Project project) {
        myProject = project;
        graphQLFilesScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), GraphQLFileType.INSTANCE);
        jsonIntrospectionScope = GlobalSearchScope
            .getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JsonFileType.INSTANCE);
        psiManager = PsiManager.getInstance(project);
        graphQLPsiSearchHelper = GraphQLPsiSearchHelper.getInstance(project);
        graphQLConfigManager = GraphQLConfigManager.getService(project);

        project.getMessageBus().connect(this).subscribe(GraphQLSchemaChangeTracker.TOPIC, scopeToRegistry::clear);
    }

    @NotNull
    public GraphQLRegistryInfo getRegistryInfo(@NotNull PsiElement scopedElement) {
        // Get the search scope that limits schema definition for the scoped element
        GlobalSearchScope schemaScope = graphQLPsiSearchHelper.getResolveScope(scopedElement);

        return scopeToRegistry.computeIfAbsent(schemaScope, s -> {
            long start = System.nanoTime();

            List<GraphQLException> errors = new ArrayList<>();
            GraphQLSchemaDocumentProcessor processor = new GraphQLSchemaDocumentProcessor();

            // GraphQL files
            FileTypeIndex.processFiles(GraphQLFileType.INSTANCE, file -> {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    processor.process(psiFile);
                }
                return true;
            }, graphQLFilesScope.intersectWith(schemaScope));

            // JSON GraphQL introspection result files
            if (!graphQLConfigManager.getConfigurationsByPath().isEmpty()) {
                // need one or more configurations to be able to point "schemaPath" to relevant JSON files
                // otherwise all JSON files would be in scope
                FileTypeIndex.processFiles(
                    JsonFileType.INSTANCE,
                    file -> processJsonFile(processor, file, errors),
                    jsonIntrospectionScope.intersectWith(schemaScope)
                );
            }

            // Injected GraphQL
            graphQLPsiSearchHelper.processInjectedGraphQLPsiFiles(scopedElement, schemaScope, processor);

            TypeDefinitionRegistry registry = processor.getCompositeRegistry().buildTypeDefinitionRegistry();

            if (LOG.isDebugEnabled()) {
                long durationMillis = TimeoutUtil.getDurationMillis(start);
                VirtualFile file = GraphQLPsiUtil.getPhysicalVirtualFile(scopedElement.getContainingFile());
                String requester = file != null ? file.getPath() : "<unknown>";
                LOG.debug(String.format("Registry build completed in %d ms, requester: %s", durationMillis, requester));
            }
            return new GraphQLRegistryInfo(registry, errors, processor.isProcessed());
        });

    }

    private boolean processJsonFile(@NotNull GraphQLSchemaDocumentProcessor processor,
                                    @NotNull VirtualFile file,
                                    @NotNull List<GraphQLException> errors) {
        // only JSON files that are directly referenced as "schemaPath" from the .graphqlconfig will be
        // considered within scope, so we can just go ahead and try to turn the JSON into GraphQL
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            return true;
        }

        try {
            processor.process(GraphQLIntrospectionFilesManager.getOrCreateIntrospectionSDL(file, psiFile));
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (SchemaProblem e) {
            errors.add(e);
        } catch (Exception e) {
            final List<SourceLocation> sourceLocation = Collections.singletonList(
                new SourceLocation(1, 1, GraphQLPsiUtil.getFileName(psiFile)));
            errors.add(new SchemaProblem(Collections.singletonList(new InvalidSyntaxError(sourceLocation, e.getMessage()))));
        }
        return true;
    }

    @Override
    public void dispose() {
    }

}
