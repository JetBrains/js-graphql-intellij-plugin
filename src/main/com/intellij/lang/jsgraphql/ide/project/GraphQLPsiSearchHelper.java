/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.GraphQLScopeResolution;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.scopes.ConditionalGlobalSearchScope;
import com.intellij.lang.jsgraphql.ide.project.scopes.GraphQLProjectScopesManager;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier;
import com.intellij.lang.jsgraphql.psi.GraphQLNamedElement;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Enables cross-file searches for PSI references
 */
public class GraphQLPsiSearchHelper {

    private static final Key<PsiFile> GRAPHQL_BUILT_IN_SCHEMA_PSI_FILE = Key.create("JSGraphQL.built-in.schema.psi-file");
    private static final Key<PsiFile> RELAY_MODERN_DIRECTIVES_SCHEMA_PSI_FILE = Key.create("JSGraphQL.relay.modern.directives.schema.psi-file");

    private final static Logger log = Logger.getInstance(GraphQLPsiSearchHelper.class);

    private final Project myProject;
    private final GraphQLSettings mySettings;
    private final PluginDescriptor pluginDescriptor;
    private final Map<String, GraphQLFragmentDefinition> fragmentDefinitionsByName = Maps.newConcurrentMap();
    private final GlobalSearchScope searchScope;
    private final GlobalSearchScope allBuiltInSchemaScopes;
    private final GraphQLConfigManager graphQLConfigManager;
    private final GraphQLProjectScopesManager graphQLProjectScopesManager;

    public static GraphQLPsiSearchHelper getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPsiSearchHelper.class);
    }


    public GraphQLPsiSearchHelper(@NotNull final Project project) {
        myProject = project;
        mySettings = GraphQLSettings.getSettings(project);
        graphQLConfigManager = GraphQLConfigManager.getService(myProject);
        graphQLProjectScopesManager = GraphQLProjectScopesManager.getService(myProject);
        pluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));

        GlobalSearchScope builtInSchemaScope = GlobalSearchScope.fileScope(project, getBuiltInSchema().getVirtualFile());
        GlobalSearchScope builtInRelaySchemaScope = GlobalSearchScope.fileScope(project, getRelayModernDirectivesSchema().getVirtualFile());
        allBuiltInSchemaScopes = builtInSchemaScope.union(new ConditionalGlobalSearchScope(builtInRelaySchemaScope, mySettings::isEnableRelayModernFrameworkSupport));

        final FileType[] searchScopeFileTypes = GraphQLFindUsagesUtil.INCLUDED_FILE_TYPES.toArray(FileType.EMPTY_ARRAY);
        searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myProject), searchScopeFileTypes).union(allBuiltInSchemaScopes);
        project.getMessageBus().connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {
                // clear the cache on each PSI change
                fragmentDefinitionsByName.clear();
            }
        });
    }

    /**
     * Uses custom editable scopes to limit the schema and reference resolution of a GraphQL psi element
     */
    public GlobalSearchScope getSchemaScope(PsiElement element) {

        final GraphQLScopeResolution scopeResolution = mySettings.getScopeResolution();

        switch (scopeResolution) {
            case PROJECT_SCOPES:
            case GRAPHQL_CONFIG_GLOBS:
                final VirtualFile virtualFile = getVirtualFile(element.getContainingFile());
                final NamedScope schemaScope = (scopeResolution == GraphQLScopeResolution.PROJECT_SCOPES
                        ? graphQLProjectScopesManager.getSchemaScope(virtualFile)
                        : graphQLConfigManager.getSchemaScope(virtualFile)
                );
                if (schemaScope != null) {
                    final GlobalSearchScope filterSearchScope = GlobalSearchScopesCore.filterScope(myProject, schemaScope);
                    return searchScope.intersectWith(filterSearchScope.union(allBuiltInSchemaScopes));
                }
                break;
        }

        // default is entire project limited by relevant file types
        return searchScope;
    }

    private static VirtualFile getVirtualFile(PsiFile containingFile) {
        VirtualFile virtualFile = containingFile.getVirtualFile();
        if (virtualFile == null) {
            // in memory PsiFile such as the completion PSI
            virtualFile = containingFile.getOriginalFile().getVirtualFile();
        }
        return virtualFile;
    }

    /**
     * Provides a search scope that indicates from where usages can occur for the specified element.
     * The main use case is for injected GraphQL where Idea defaults to the current file only.
     */
    public GlobalSearchScope getUseScope(PsiElement element) {
        if (element.getContainingFile().getVirtualFile() != null) {
            return getSchemaScope(element);
        } else {
            return searchScope;
        }
    }

    /**
     * Finds all fragment definition across files in the project
     *
     * @param scopedElement the starting point for finding known fragment definitions
     *
     * @return a list of known fragment definitions, or an empty list if the index is not yet ready
     */
    public List<GraphQLFragmentDefinition> getKnownFragmentDefinitions(PsiElement scopedElement) {
        try {
            final List<GraphQLFragmentDefinition> fragmentDefinitions = Lists.newArrayList();
            PsiSearchHelper.SERVICE.getInstance(myProject).processElementsWithWord((psiElement, offsetInElement) -> {
                if (psiElement.getNode().getElementType() == GraphQLElementTypes.FRAGMENT_KEYWORD) {
                    final GraphQLFragmentDefinition fragmentDefinition = PsiTreeUtil.getParentOfType(psiElement, GraphQLFragmentDefinition.class);
                    if (fragmentDefinition != null && fragmentDefinition.getNameIdentifier() != null) {
                        fragmentDefinitions.add(fragmentDefinition);
                    }
                }
                return true;
            }, getSchemaScope(scopedElement), "fragment", UsageSearchContext.IN_CODE, true, true);
            return fragmentDefinitions;
        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
        return Collections.emptyList();
    }

    /**
     * Gets a resolved reference or null if no reference or resolved element is found
     *
     * @param psiElement the element to get a resolved reference for
     *
     * @return the resolved reference, or null if non is available
     */
    public static GraphQLIdentifier getResolvedReference(GraphQLNamedElement psiElement) {
        if (psiElement != null) {
            final PsiElement nameIdentifier = psiElement.getNameIdentifier();
            if (nameIdentifier != null) {
                PsiReference reference = nameIdentifier.getReference();
                if (reference != null) {
                    PsiElement resolved = reference.resolve();
                    return resolved instanceof GraphQLIdentifier ? (GraphQLIdentifier) resolved : null;
                }
            }
        }
        return null;
    }

    /**
     * Processes all named elements that match the specified word, e.g. the declaration of a type name
     */
    public void processElementsWithWord(PsiElement scopedElement, String word, Predicate<PsiNamedElement> predicate) {
        try {
            final GlobalSearchScope schemaScope = getSchemaScope(scopedElement);
            PsiSearchHelper.SERVICE.getInstance(myProject).processElementsWithWord((psiElement, offsetInElement) -> {
                if (psiElement instanceof PsiNamedElement) {
                    return predicate.test((PsiNamedElement) psiElement);
                }
                return true;
            }, schemaScope, word, UsageSearchContext.IN_CODE, true, true);

            // also include the built-in schemas
            final PsiRecursiveElementVisitor builtInFileVisitor = new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (element instanceof PsiNamedElement && word.equals(element.getText())) {
                        predicate.test((PsiNamedElement) element);
                    }
                    super.visitElement(element);
                }
            };

            // spec schema
            getBuiltInSchema().accept(builtInFileVisitor);

            // relay schema if enabled
            final PsiFile relayModernDirectivesSchema = getRelayModernDirectivesSchema();
            if (schemaScope.contains(relayModernDirectivesSchema.getVirtualFile())) {
                relayModernDirectivesSchema.accept(builtInFileVisitor);
            }

        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
    }

    /**
     * Gets the built-in Schema that all endpoints support, including the introspection types, fields, directives and default scalars.
     */
    public PsiFile getBuiltInSchema() {
        return getGraphQLPsiFileFromResources(
                "graphql specification schema.graphql",
                "GraphQL Specification Schema",
                GRAPHQL_BUILT_IN_SCHEMA_PSI_FILE
        );
    }

    /**
     * Gets the built-in Relay Modern Directives schema
     */
    public PsiFile getRelayModernDirectivesSchema() {
        return getGraphQLPsiFileFromResources(
                "relay modern directives schema.graphql",
                "Relay Modern Directives Schema",
                RELAY_MODERN_DIRECTIVES_SCHEMA_PSI_FILE
        );
    }

    private PsiFile getGraphQLPsiFileFromResources(String resourceName, String displayName, Key<PsiFile> cacheProjectKey) {
        PsiFile psiFile = myProject.getUserData(cacheProjectKey);
        if (psiFile == null) {
            final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
            String specSchemaText = "";
            try {
                try (InputStream inputStream = pluginDescriptor.getPluginClassLoader().getResourceAsStream("/META-INF/" + resourceName)) {
                    if (inputStream != null) {
                        specSchemaText = new String(IOUtils.toByteArray(inputStream));
                    }
                }
            } catch (IOException e) {
                log.error("Unable to load schema", e);
                Notifications.Bus.notify(new Notification("GraphQL", "Unable to load " + displayName, e.getMessage(), NotificationType.ERROR));
            }
            psiFile = psiFileFactory.createFileFromText(displayName, GraphQLLanguage.INSTANCE, specSchemaText);
            myProject.putUserData(cacheProjectKey, psiFile);
            try {
                psiFile.getVirtualFile().setWritable(false);
            } catch (IOException ignored) {
            }
        }
        return psiFile;
    }

    /**
     * Process injected GraphQL PsiFiles
     *
     * @param scopedElement the starting point of the enumeration settings the scopedElement of the processing
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param consumer      a consumer that will be invoked for each injected GraphQL PsiFile
     */
    public void processInjectedGraphQLPsiFiles(PsiElement scopedElement, GlobalSearchScope schemaScope, Consumer<PsiFile> consumer) {
        GraphQLInjectionSearchHelper graphQLInjectionSearchHelper = ServiceManager.getService(myProject, GraphQLInjectionSearchHelper.class);
        if (graphQLInjectionSearchHelper != null) {
            graphQLInjectionSearchHelper.processInjectedGraphQLPsiFiles(scopedElement, schemaScope, consumer);
        }
    }

    /**
     * Process built-in GraphQL PsiFiles that are not the spec schema
     *
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param consumer      a consumer that will be invoked for each injected GraphQL PsiFile
     */
    public void processAdditionalBuiltInPsiFiles(GlobalSearchScope schemaScope, Consumer<PsiFile> consumer) {
        final PsiFile relayModernDirectivesSchema = getRelayModernDirectivesSchema();
        if(schemaScope.contains(relayModernDirectivesSchema.getVirtualFile())) {
            consumer.accept(relayModernDirectivesSchema);
        }
    }

    /**
     * Gets the virtual file system path of a PSI file
     */
    public static String getFileName(PsiFile psiFile) {
        VirtualFile virtualFile = getVirtualFile(psiFile);
        if (virtualFile != null) {
            return virtualFile.getPath();
        }
        return psiFile.getName();
    }

}
