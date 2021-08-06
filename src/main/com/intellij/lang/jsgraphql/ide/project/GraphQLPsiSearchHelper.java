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
import com.intellij.lang.jsgraphql.schema.GraphQLExternalTypeDefinitionsProvider;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaKeys;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enables cross-file searches for PSI references
 */
public class GraphQLPsiSearchHelper implements Disposable {

    private final Project myProject;
    private final Map<String, GlobalSearchScope> myFileNameToSchemaScope = Maps.newConcurrentMap();
    private final GlobalSearchScope myGlobalScope;
    private final GlobalSearchScope myBuiltInSchemaScopes;
    private final GraphQLConfigManager myConfigManager;

    private final GraphQLFile myDefaultProjectFile;
    private final PsiManager myPsiManager;
    private final @Nullable GraphQLInjectionSearchHelper myInjectionSearchHelper;
    private final InjectedLanguageManager myInjectedLanguageManager;
    private final GraphQLExternalTypeDefinitionsProvider myExternalDefinitionsProvider;

    public static GraphQLPsiSearchHelper getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPsiSearchHelper.class);
    }

    public GraphQLPsiSearchHelper(@NotNull final Project project) {
        myProject = project;
        myPsiManager = PsiManager.getInstance(myProject);
        myInjectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
        myInjectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
        myConfigManager = GraphQLConfigManager.getService(myProject);
        myExternalDefinitionsProvider = GraphQLExternalTypeDefinitionsProvider.getInstance(myProject);

        final PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(myProject);
        myDefaultProjectFile = (GraphQLFile) psiFileFactory.createFileFromText("Default schema file", GraphQLLanguage.INSTANCE, "");

        GlobalSearchScope defaultProjectFileScope = GlobalSearchScope.fileScope(myDefaultProjectFile);
        GlobalSearchScope builtInSchemaScope =
            GlobalSearchScope.fileScope(project, myExternalDefinitionsProvider.getBuiltInSchema().getVirtualFile());
        GlobalSearchScope builtInRelaySchemaScope =
            GlobalSearchScope.fileScope(project, myExternalDefinitionsProvider.getRelayModernDirectivesSchema().getVirtualFile());

        GraphQLSettings settings = GraphQLSettings.getSettings(project);
        myBuiltInSchemaScopes = builtInSchemaScope
            .union(new ConditionalGlobalSearchScope(builtInRelaySchemaScope, settings::isEnableRelayModernFrameworkSupport))
            .union(defaultProjectFileScope);

        final FileType[] searchScopeFileTypes = GraphQLFindUsagesUtil.getService().getIncludedFileTypes().toArray(FileType.EMPTY_ARRAY);
        myGlobalScope = GlobalSearchScope
            .getScopeRestrictedByFileTypes(GlobalSearchScope.projectScope(myProject), searchScopeFileTypes)
            .union(myBuiltInSchemaScopes);

        project.getMessageBus().connect(this).subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener() {
            @Override
            public void beforePsiChanged(boolean isPhysical) {
                // clear the cache on each PSI change
                myFileNameToSchemaScope.clear();
            }

            @Override
            public void afterPsiChanged(boolean isPhysical) {
            }
        });
    }

    /**
     * Gets an empty GraphQL file that can be used to get a single project-wide schema scope.
     */
    @NotNull
    public GraphQLFile getDefaultProjectFile() {
        return myDefaultProjectFile;
    }

    /**
     * Uses custom editable scopes to limit the schema and reference resolution of a GraphQL psi element
     */
    @NotNull
    public GlobalSearchScope getSchemaScope(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        return myFileNameToSchemaScope.computeIfAbsent(GraphQLPsiUtil.getFileName(containingFile), fileName -> {
            final VirtualFile virtualFile = GraphQLPsiUtil.getOriginalVirtualFile(containingFile);
            final NamedScope schemaScope = myConfigManager.getSchemaScope(virtualFile);
            if (schemaScope != null) {
                final GlobalSearchScope filterSearchScope = GlobalSearchScopesCore.filterScope(myProject, schemaScope);
                return myGlobalScope.intersectWith(filterSearchScope.union(myBuiltInSchemaScopes));
            }

            // default is entire project limited by relevant file types
            return myGlobalScope;
        });
    }

    /**
     * Provides a search scope that indicates from where usages can occur for the specified element.
     * The main use case is for injected GraphQL where Idea defaults to the current file only.
     */
    @NotNull
    public GlobalSearchScope getUseScope(@NotNull PsiElement element) {
        if (element.getContainingFile().getVirtualFile() != null) {
            return getSchemaScope(element);
        } else {
            return myGlobalScope;
        }
    }

    /**
     * Finds all fragment definitions inside the scope of the specified element
     *
     * @param scopedElement the starting point for finding known fragment definitions
     * @return a list of known fragment definitions, or an empty list if the index is not yet ready
     */
    @NotNull
    public List<GraphQLFragmentDefinition> getKnownFragmentDefinitions(@NotNull PsiElement scopedElement) {
        try {
            final List<GraphQLFragmentDefinition> fragmentDefinitions = Lists.newArrayList();
            GlobalSearchScope schemaScope = getSchemaScope(scopedElement);
            VirtualFile originalFile = GraphQLPsiUtil.getOriginalVirtualFile(scopedElement.getContainingFile());
            if (originalFile != null && GraphQLFileType.isGraphQLScratchFile(myProject, originalFile)) {
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
                        public void visitElement(@NotNull PsiElement element) {
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
    private boolean visitLanguageInjectionHost(@NotNull PsiLanguageInjectionHost element,
                                               @NotNull Ref<PsiRecursiveElementVisitor> identifierVisitor) {
        if (myInjectionSearchHelper != null && myInjectionSearchHelper.isGraphQLLanguageInjectionTarget(element)) {
            myInjectedLanguageManager.enumerateEx(
                element, element.getContainingFile(), false,
                (injectedPsi, places) -> injectedPsi.accept(identifierVisitor.get())
            );
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
    @Nullable
    public static GraphQLIdentifier getResolvedReference(@Nullable GraphQLNamedElement psiElement) {
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
    private void processElementsWithWordUsingIdentifierIndex(@NotNull GlobalSearchScope schemaScope,
                                                             @NotNull String word,
                                                             @NotNull Processor<PsiNamedElement> processor) {
        FileBasedIndex.getInstance().getFilesWithKey(GraphQLIdentifierIndex.NAME, Collections.singleton(word), virtualFile -> {
            final PsiFile psiFile = myPsiManager.findFile(virtualFile);
            final Ref<Boolean> continueProcessing = Ref.create(true);
            if (psiFile != null) {
                final Set<GraphQLFile> introspectionFiles = Sets.newHashSetWithExpectedSize(1);
                final Ref<PsiRecursiveElementVisitor> identifierVisitor = Ref.create();
                identifierVisitor.set(new PsiRecursiveElementVisitor() {
                    @Override
                    public void visitElement(@NotNull PsiElement element) {
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
                            final CachedValue<GraphQLFile> cachedFile = element.getContainingFile().getUserData(GraphQLSchemaKeys.GRAPHQL_INTROSPECTION_JSON_TO_SDL);
                            if (cachedFile != null && cachedFile.hasUpToDateValue()) {
                                GraphQLFile graphQLFile = cachedFile.getValue();
                                if (introspectionFiles.add(graphQLFile)) {
                                    // index the associated introspection SDL from a JSON introspection result file
                                    graphQLFile.accept(identifierVisitor.get());
                                }
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
    public void processElementsWithWord(@NotNull PsiElement scopedElement,
                                        @NotNull String word,
                                        @NotNull Processor<PsiNamedElement> processor) {
        try {
            final GlobalSearchScope schemaScope = getSchemaScope(scopedElement);

            processElementsWithWordUsingIdentifierIndex(schemaScope, word, processor);

            // also include the built-in schemas
            final PsiRecursiveElementVisitor builtInFileVisitor = new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PsiNamedElement && word.equals(element.getText())) {
                        if (!processor.process((PsiNamedElement) element)) {
                            return; // done processing
                        }
                    }
                    super.visitElement(element);
                }
            };

            // spec schema
            myExternalDefinitionsProvider.getBuiltInSchema().accept(builtInFileVisitor);

            // relay schema if enabled
            final PsiFile relayModernDirectivesSchema = myExternalDefinitionsProvider.getRelayModernDirectivesSchema();
            if (schemaScope.contains(relayModernDirectivesSchema.getVirtualFile())) {
                relayModernDirectivesSchema.accept(builtInFileVisitor);
            }

            // finally, look in the current scratch file
            PsiFile containingFile = scopedElement.getContainingFile();
            VirtualFile originalVirtualFile = GraphQLPsiUtil.getOriginalVirtualFile(containingFile);
            if (originalVirtualFile != null && GraphQLFileType.isGraphQLScratchFile(myProject, originalVirtualFile)) {
                containingFile.accept(builtInFileVisitor);
            }

        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
    }

    /**
     * Process injected GraphQL PsiFiles
     *
     * @param scopedElement the starting point of the enumeration settings the scopedElement of the processing
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param processor     a processor that will be invoked for each injected GraphQL PsiFile
     */
    public void processInjectedGraphQLPsiFiles(@NotNull PsiElement scopedElement,
                                               @NotNull GlobalSearchScope schemaScope,
                                               @NotNull Processor<PsiFile> processor) {
        if (myInjectionSearchHelper != null) {
            myInjectionSearchHelper.processInjectedGraphQLPsiFiles(scopedElement, schemaScope, processor);
        }
    }

    /**
     * Process built-in GraphQL PsiFiles that are not the spec schema
     *
     * @param schemaScope the search scope to use for limiting the schema definitions
     * @param processor   a processor that will be invoked for each injected GraphQL PsiFile
     */
    public void processAdditionalBuiltInPsiFiles(@NotNull GlobalSearchScope schemaScope, @NotNull Processor<PsiFile> processor) {
        final PsiFile relayModernDirectivesSchema = myExternalDefinitionsProvider.getRelayModernDirectivesSchema();
        if (schemaScope.contains(relayModernDirectivesSchema.getVirtualFile())) {
            processor.process(relayModernDirectivesSchema);
        }
    }

    @Override
    public void dispose() {
    }
}
