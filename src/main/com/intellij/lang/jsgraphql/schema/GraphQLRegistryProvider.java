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
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.endpoint.ide.project.JSGraphQLEndpointNamedTypeRegistry;
import com.intellij.lang.jsgraphql.ide.editor.GraphQLIntrospectionService;
import com.intellij.lang.jsgraphql.ide.project.GraphQLPsiSearchHelper;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
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
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys.*;

public class GraphQLRegistryProvider implements Disposable {

    private static final Logger LOG = Logger.getInstance(GraphQLRegistryProvider.class);

    private final GraphQLPsiSearchHelper graphQLPsiSearchHelper;
    private final Project project;
    private final GlobalSearchScope graphQLFilesScope;
    private final GlobalSearchScope jsonIntrospectionScope;
    private final PsiManager psiManager;
    private final JSGraphQLEndpointNamedTypeRegistry graphQLEndpointNamedTypeRegistry;
    private final GraphQLConfigManager graphQLConfigManager;
    private final GraphQLSettings mySettings;

    private final Map<GlobalSearchScope, GraphQLRegistryInfo> scopeToRegistry = Maps.newConcurrentMap();

    public static GraphQLRegistryProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLRegistryProvider.class);
    }

    public GraphQLRegistryProvider(Project project) {
        this.project = project;
        graphQLFilesScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), GraphQLFileType.INSTANCE);
        jsonIntrospectionScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(project), JsonFileType.INSTANCE);
        psiManager = PsiManager.getInstance(project);
        graphQLEndpointNamedTypeRegistry = JSGraphQLEndpointNamedTypeRegistry.getService(project);
        graphQLPsiSearchHelper = GraphQLPsiSearchHelper.getInstance(project);
        graphQLConfigManager = GraphQLConfigManager.getService(project);
        mySettings = GraphQLSettings.getSettings(project);

        project.getMessageBus().connect(this).subscribe(GraphQLSchemaChangeListener.TOPIC, schemaVersion -> {
            scopeToRegistry.clear();
        });
    }

    @NotNull
    public GraphQLRegistryInfo getRegistryInfo(@NotNull PsiElement scopedElement) {
        // Get the search scope that limits schema definition for the scoped element
        GlobalSearchScope schemaScope = graphQLPsiSearchHelper.getSchemaScope(scopedElement);

        return scopeToRegistry.computeIfAbsent(schemaScope, s -> {
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

            // Built-in that are additions to a default registry which already has the GraphQL spec directives
            graphQLPsiSearchHelper.processAdditionalBuiltInPsiFiles(schemaScope, processor);

            // Types defined using GraphQL Endpoint Language
            VirtualFile virtualFile = GraphQLPsiUtil.getPhysicalVirtualFile(scopedElement.getContainingFile());
            if (virtualFile != null && graphQLConfigManager.getEndpointLanguageConfiguration(virtualFile, null) != null) {
                final GraphQLRegistryInfo endpointTypesAsRegistry = graphQLEndpointNamedTypeRegistry.getTypesAsRegistry(scopedElement);
                try {
                    processor.getCompositeRegistry().merge(endpointTypesAsRegistry.getTypeDefinitionRegistry());
                    errors.addAll(endpointTypesAsRegistry.getErrors());
                } catch (GraphQLException e) {
                    errors.add(e);
                }
            }

            TypeDefinitionRegistry registry = processor.getCompositeRegistry().buildTypeDefinitionRegistry();
            return new GraphQLRegistryInfo(registry, errors, processor.isProcessed());
        });

    }

    private boolean processJsonFile(GraphQLSchemaDocumentProcessor processor,
                                    VirtualFile file,
                                    List<GraphQLException> errors) {
        // only JSON files that are directly referenced as "schemaPath" from the .graphqlconfig will be
        // considered within scope, so we can just go ahead and try to turn the JSON into GraphQL
        final PsiFile psiFile = psiManager.findFile(file);
        if (psiFile == null) {
            return true;
        }

        try {
            synchronized (GRAPHQL_INTROSPECTION_JSON_TO_SDL) {
                GraphQLFile introspectionSDL = CachedValuesManager.getCachedValue(psiFile, GRAPHQL_INTROSPECTION_JSON_TO_SDL, () -> {
                    final String introspectionJsonAsGraphQL =
                        GraphQLIntrospectionService.getInstance(project).printIntrospectionAsGraphQL(psiFile.getText());
                    final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                    final String fileName = file.getPath();
                    final GraphQLFile newIntrospectionFile =
                        (GraphQLFile) psiFileFactory.createFileFromText(fileName, GraphQLLanguage.INSTANCE, introspectionJsonAsGraphQL);
                    newIntrospectionFile.putUserData(IS_GRAPHQL_INTROSPECTION_SDL, true);
                    newIntrospectionFile.putUserData(GRAPHQL_INTROSPECTION_SDL_TO_JSON, psiFile);
                    newIntrospectionFile.getVirtualFile().putUserData(IS_GRAPHQL_INTROSPECTION_SDL, true);
                    newIntrospectionFile.getVirtualFile().putUserData(GRAPHQL_INTROSPECTION_SDL_TO_JSON, psiFile);
                    try {
                        newIntrospectionFile.getVirtualFile().setWritable(false);
                    } catch (IOException e) {
                        LOG.warn(e);
                    }
                    return new CachedValueProvider.Result<>(newIntrospectionFile, psiFile, mySettings.getSchemaSettingsModificationTracker());
                });

                processor.process(introspectionSDL);
            }
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (SchemaProblem e) {
            errors.add(e);
        } catch (Exception e) {
            final List<SourceLocation> sourceLocation = Collections.singletonList(new SourceLocation(1, 1, GraphQLPsiUtil.getFileName(psiFile)));
            errors.add(new SchemaProblem(Collections.singletonList(new InvalidSyntaxError(sourceLocation, e.getMessage()))));
        }
        return true;
    }

    @Override
    public void dispose() {
    }

}
