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
import com.google.common.collect.Sets;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.project.indexing.GraphQLFragmentNameIndex;
import com.intellij.lang.jsgraphql.ide.project.indexing.GraphQLIdentifierIndex;
import com.intellij.lang.jsgraphql.ide.project.scopes.ConditionalGlobalSearchScope;
import com.intellij.lang.jsgraphql.ide.references.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.psi.*;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
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
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.apache.commons.compress.utils.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
    private final Map<String, GlobalSearchScope> fileNameToSchemaScope = Maps.newConcurrentMap();
    private final GlobalSearchScope searchScope;
    private final GlobalSearchScope allBuiltInSchemaScopes;
    private final GraphQLConfigManager graphQLConfigManager;

    private GraphQLFile defaultProjectFile;
    private PsiManager psiManager;
    private GraphQLInjectionSearchHelper graphQLInjectionSearchHelper;
    private final InjectedLanguageManager injectedLanguageManager;

    public static GraphQLPsiSearchHelper getService(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPsiSearchHelper.class);
    }


    public GraphQLPsiSearchHelper(@NotNull final Project project) {
        myProject = project;
        mySettings = GraphQLSettings.getSettings(project);
        psiManager = PsiManager.getInstance(myProject);
        graphQLInjectionSearchHelper = ServiceManager.getService(GraphQLInjectionSearchHelper.class);
        injectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
        graphQLConfigManager = GraphQLConfigManager.getService(myProject);
        pluginDescriptor = PluginManager.getPlugin(PluginId.getId("com.intellij.lang.jsgraphql"));

        final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
        defaultProjectFile = (GraphQLFile) psiFileFactory.createFileFromText("Default schema file", GraphQLLanguage.INSTANCE, "");

        GlobalSearchScope defaultProjectFileScope = GlobalSearchScope.fileScope(defaultProjectFile);
        GlobalSearchScope builtInSchemaScope = GlobalSearchScope.fileScope(project, getBuiltInSchema().getVirtualFile());
        GlobalSearchScope builtInRelaySchemaScope = GlobalSearchScope.fileScope(project, getRelayModernDirectivesSchema().getVirtualFile());
        allBuiltInSchemaScopes = builtInSchemaScope
            .union(new ConditionalGlobalSearchScope(builtInRelaySchemaScope, mySettings::isEnableRelayModernFrameworkSupport))
            .union(defaultProjectFileScope)
        ;

        final FileType[] searchScopeFileTypes = GraphQLFindUsagesUtil.getService().getIncludedFileTypes().toArray(FileType.EMPTY_ARRAY);
        searchScope = GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myProject), searchScopeFileTypes).union(allBuiltInSchemaScopes);
        project.getMessageBus().connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {
                // clear the cache on each PSI change
                fileNameToSchemaScope.clear();
            }
        });
    }

    /**
     * Gets an empty GraphQL file that can be used to get a single project-wide schema scope.
     */
    public GraphQLFile getDefaultProjectFile() {
        return defaultProjectFile;
    }

    /**
     * Uses custom editable scopes to limit the schema and reference resolution of a GraphQL psi element
     */
    public GlobalSearchScope getSchemaScope(PsiElement element) {

        return fileNameToSchemaScope.computeIfAbsent(GraphQLPsiUtil.getFileName(element.getContainingFile()), fileName -> {

            final VirtualFile virtualFile = GraphQLPsiUtil.getVirtualFileFromPsiFile(element.getContainingFile());
            final NamedScope schemaScope = graphQLConfigManager.getSchemaScope(virtualFile);
            if (schemaScope != null) {
                final GlobalSearchScope filterSearchScope = GlobalSearchScopesCore.filterScope(myProject, schemaScope);
                return searchScope.intersectWith(filterSearchScope.union(allBuiltInSchemaScopes));
            }

            // default is entire project limited by relevant file types
            return searchScope;

        });

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
     * Finds all fragment definitions inside the scope of the specified element
     *
     * @param scopedElement the starting point for finding known fragment definitions
     * @return a list of known fragment definitions, or an empty list if the index is not yet ready
     */
    public List<GraphQLFragmentDefinition> getKnownFragmentDefinitions(PsiElement scopedElement) {
        try {
            final List<GraphQLFragmentDefinition> fragmentDefinitions = Lists.newArrayList();
            GlobalSearchScope schemaScope = getSchemaScope(scopedElement);
            if (GraphQLFileType.isGraphQLScratchFile(myProject, GraphQLPsiUtil.getVirtualFileFromPsiFile(scopedElement.getContainingFile()))) {
                // include the fragments defined in the currently edited scratch file (scratch files don't appear to be indexed)
                fragmentDefinitions.addAll(PsiTreeUtil.getChildrenOfTypeAsList(scopedElement.getContainingFile().getOriginalFile(), GraphQLFragmentDefinition.class));
            }

            final PsiManager psiManager = PsiManager.getInstance(myProject);

            FileBasedIndex.getInstance().processFilesContainingAllKeys(GraphQLFragmentNameIndex.NAME, Collections.singleton(GraphQLFragmentNameIndex.HAS_FRAGMENTS), schemaScope, null, virtualFile -> {

                final PsiFile psiFile = psiManager.findFile(virtualFile);
                if (psiFile != null) {
                    final Ref<PsiRecursiveElementVisitor> identifierVisitor = Ref.create();
                    identifierVisitor.set(new PsiRecursiveElementVisitor() {
                        @Override
                        public void visitElement(PsiElement element) {
                            if (element instanceof GraphQLDefinition) {
                                if (element instanceof GraphQLFragmentDefinition) {
                                    fragmentDefinitions.add((GraphQLFragmentDefinition) element);
                                }
                                return; // no need to visit deeper than definitions since fragments are top level
                            } else if (element instanceof PsiLanguageInjectionHost) {
                                if (visitLanguageInjectionHost((PsiLanguageInjectionHost) element, identifierVisitor)) {
                                    return;
                                }
                            }
                            super.visitElement(element);
                        }
                    });
                    psiFile.accept(identifierVisitor.get());
                }

                return true; // process all known fragments
            });
            return fragmentDefinitions;
        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
        return Collections.emptyList();
    }

    /**
     * Visits the potential GraphQL injection inside an injection host
     *
     * @return true if the host contained GraphQL and was visited, false otherwise
     */
    private boolean visitLanguageInjectionHost(PsiLanguageInjectionHost element, Ref<PsiRecursiveElementVisitor> identifierVisitor) {
        if (graphQLInjectionSearchHelper != null && graphQLInjectionSearchHelper.isJSGraphQLLanguageInjectionTarget(element)) {
            injectedLanguageManager.enumerateEx(element, element.getContainingFile(), false, (injectedPsi, places) -> {
                injectedPsi.accept(identifierVisitor.get());
            });
            return true;
        }
        return false;
    }

    /**
     * Gets a resolved reference or null if no reference or resolved element is found
     *
     * @param psiElement the element to get a resolved reference for
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
     * Processes GraphQL identifiers whose name matches the specified word within the given schema scope.
     *
     * @param schemaScope the schema scope which limits the processing
     * @param word        the word to match identifiers for
     * @param processor   processor called for all GraphQL identifiers whose name match the specified word
     * @see GraphQLIdentifierIndex
     */
    private void processElementsWithWordUsingIdentifierIndex(GlobalSearchScope schemaScope, String word, Processor<PsiNamedElement> processor) {
        FileBasedIndex.getInstance().getFilesWithKey(GraphQLIdentifierIndex.NAME, Collections.singleton(word), virtualFile -> {
            final PsiFile psiFile = psiManager.findFile(virtualFile);
            final Ref<Boolean> continueProcessing = Ref.create(true);
            if (psiFile != null) {
                final Set<GraphQLFile> introspectionFiles = Sets.newHashSetWithExpectedSize(1);
                final Ref<PsiRecursiveElementVisitor> identifierVisitor = Ref.create();
                identifierVisitor.set(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        if (!continueProcessing.get()) {
                            return; // done visiting as the processor returned false
                        }
                        if (element instanceof PsiNamedElement) {
                            final String name = ((PsiNamedElement) element).getName();
                            if (word.equals(name)) {
                                // found an element with a name that matches
                                continueProcessing.set(processor.process((PsiNamedElement) element));
                            }
                            if (!continueProcessing.get()) {
                                return; // no need to visit other elements
                            }
                        } else if (element instanceof JsonStringLiteral) {
                            final GraphQLFile graphQLFile = element.getContainingFile().getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_JSON_TO_SDL);
                            if (graphQLFile != null && introspectionFiles.add(graphQLFile)) {
                                // index the associated introspection SDL from a JSON introspection result file
                                graphQLFile.accept(identifierVisitor.get());
                            }
                            return; // no need to visit deeper
                        } else if (element instanceof PsiLanguageInjectionHost) {
                            if (visitLanguageInjectionHost((PsiLanguageInjectionHost) element, identifierVisitor)) {
                                return;
                            }
                        }
                        super.visitElement(element);
                    }
                });

                psiFile.accept(identifierVisitor.get());
            }
            return continueProcessing.get();
        }, schemaScope);
    }

    /**
     * Processes all named elements that match the specified word, e.g. the declaration of a type name
     */
    public void processElementsWithWord(PsiElement scopedElement, String word, Processor<PsiNamedElement> processor) {
        try {
            final GlobalSearchScope schemaScope = getSchemaScope(scopedElement);

            processElementsWithWordUsingIdentifierIndex(schemaScope, word, processor);

            // also include the built-in schemas
            final PsiRecursiveElementVisitor builtInFileVisitor = new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    if (element instanceof PsiNamedElement && word.equals(element.getText())) {
                        if (!processor.process((PsiNamedElement) element)) {
                            return; // done processing
                        }
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

            // finally, look in the current scratch file
            if (GraphQLFileType.isGraphQLScratchFile(myProject, GraphQLPsiUtil.getVirtualFileFromPsiFile(scopedElement.getContainingFile()))) {
                scopedElement.getContainingFile().accept(builtInFileVisitor);
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
        if (graphQLInjectionSearchHelper != null) {
            graphQLInjectionSearchHelper.processInjectedGraphQLPsiFiles(scopedElement, schemaScope, consumer);
        }
    }

    /**
     * Process built-in GraphQL PsiFiles that are not the spec schema
     *
     * @param schemaScope the search scope to use for limiting the schema definitions
     * @param consumer    a consumer that will be invoked for each injected GraphQL PsiFile
     */
    public void processAdditionalBuiltInPsiFiles(GlobalSearchScope schemaScope, Consumer<PsiFile> consumer) {
        final PsiFile relayModernDirectivesSchema = getRelayModernDirectivesSchema();
        if (schemaScope.contains(relayModernDirectivesSchema.getVirtualFile())) {
            consumer.accept(relayModernDirectivesSchema);
        }
    }

}
