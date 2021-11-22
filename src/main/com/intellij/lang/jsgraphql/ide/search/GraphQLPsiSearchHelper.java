/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.search;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.lang.jsgraphql.ide.findUsages.GraphQLFindUsagesUtil;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLFragmentNameIndex;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLIdentifierIndex;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionFilesManager;
import com.intellij.lang.jsgraphql.ide.project.graphqlconfig.GraphQLConfigManager;
import com.intellij.lang.jsgraphql.ide.search.scope.GraphQLMetaInfSchemaSearchScope;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaChangeTracker;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryRootsProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;
import com.intellij.openapi.util.registry.RegistryValueListener;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enables cross-file searches for PSI references
 */
public class GraphQLPsiSearchHelper implements Disposable {

    private static final String GRAPHQL_SEARCH_SCOPE_LIBRARIES_KEY = "graphql.search.scope.libraries";

    private final Project myProject;
    private final GlobalSearchScope myDefaultProjectFileScope;
    private final GraphQLConfigManager myConfigManager;

    private final Map<String, GlobalSearchScope> myFileNameToSchemaScope = Maps.newConcurrentMap();
    private final GraphQLFile myDefaultProjectFile;
    private final PsiManager myPsiManager;
    private final @Nullable GraphQLInjectionSearchHelper myInjectionSearchHelper;
    private final InjectedLanguageManager myInjectedLanguageManager;
    // enabled by default, can be disabled using the registry key in case of any problems on the user's side
    private volatile boolean myShouldSearchInLibraries;

    public static GraphQLPsiSearchHelper getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPsiSearchHelper.class);
    }

    public GraphQLPsiSearchHelper(@NotNull final Project project) {
        myProject = project;
        myPsiManager = PsiManager.getInstance(myProject);
        myInjectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
        myInjectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
        myConfigManager = GraphQLConfigManager.getService(myProject);

        // TODO: [vepanimas] CachedValuesManager reports this as a memory leak and throws an error in tests
        myDefaultProjectFile = (GraphQLFile) PsiFileFactory.getInstance(myProject).createFileFromText("Default schema file",
            GraphQLLanguage.INSTANCE, "");
        myDefaultProjectFileScope = GlobalSearchScope.fileScope(myDefaultProjectFile);

        myShouldSearchInLibraries = Registry.is(GRAPHQL_SEARCH_SCOPE_LIBRARIES_KEY);
        Registry.get(GRAPHQL_SEARCH_SCOPE_LIBRARIES_KEY).addListener(new RegistryValueListener() {
            @Override
            public void afterValueChanged(@NotNull RegistryValue value) {
                Application app = ApplicationManager.getApplication();
                app.invokeLater(() -> app.runWriteAction(() -> {
                    myShouldSearchInLibraries = value.asBoolean();
                    PsiManager.getInstance(myProject).dropPsiCaches();
                    DaemonCodeAnalyzer.getInstance(myProject).restart();
                    GraphQLSchemaChangeTracker.getInstance(myProject).schemaChanged();
                }), ModalityState.NON_MODAL, myProject.getDisposed());
            }
        }, this);

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

    @NotNull
    private GlobalSearchScope createScope(@Nullable GlobalSearchScope configRestrictedScope) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(myProject);
        if (configRestrictedScope != null) {
            scope = scope.intersectWith(configRestrictedScope);
        }

        // these scopes are used unconditionally, both for global and config filtered scopes
        scope = scope
            .union(myDefaultProjectFileScope)
            .union(createExternalDefinitionsLibraryScope());

        if (myShouldSearchInLibraries) {
            scope = scope.union(new GraphQLMetaInfSchemaSearchScope(myProject));
        }

        // filter all the resulting scopes by file types, we don't want some child scope to override this
        FileType[] fileTypes = GraphQLFindUsagesUtil.getService().getIncludedFileTypes().toArray(FileType.EMPTY_ARRAY);
        return GlobalSearchScope.getScopeRestrictedByFileTypes(scope, fileTypes);
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
    public GlobalSearchScope getResolveScope(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        return myFileNameToSchemaScope.computeIfAbsent(GraphQLPsiUtil.getFileName(file), fileName -> {
            final VirtualFile virtualFile = GraphQLPsiUtil.getOriginalVirtualFile(file);
            final NamedScope configRestrictedScope = myConfigManager.getSchemaScope(virtualFile);

            return configRestrictedScope != null
                ? createScope(GlobalSearchScopesCore.filterScope(myProject, configRestrictedScope))
                : createScope(null);
        });
    }

    /**
     * Finds all fragment definitions inside the scope of the specified element
     *
     * @param context the starting point for finding known fragment definitions
     * @return a list of known fragment definitions, or an empty list if the index is not yet ready
     */
    @NotNull
    public List<GraphQLFragmentDefinition> getKnownFragmentDefinitions(@NotNull PsiElement context) {
        try {
            final List<GraphQLFragmentDefinition> fragmentDefinitions = Lists.newArrayList();
            GlobalSearchScope schemaScope = getResolveScope(context);
            VirtualFile originalFile = GraphQLPsiUtil.getOriginalVirtualFile(context.getContainingFile());
            if (originalFile != null && GraphQLFileType.isGraphQLScratchFile(myProject, originalFile)) {
                // include the fragments defined in the currently edited scratch file (scratch files don't appear to be indexed)
                fragmentDefinitions.addAll(PsiTreeUtil.getChildrenOfTypeAsList(context.getContainingFile().getOriginalFile(),
                    GraphQLFragmentDefinition.class));
            }

            final PsiManager psiManager = PsiManager.getInstance(myProject);

            FileBasedIndex.getInstance().processFilesContainingAllKeys(GraphQLFragmentNameIndex.NAME,
                Collections.singleton(GraphQLFragmentNameIndex.HAS_FRAGMENTS), schemaScope, null, virtualFile -> {

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
                            GraphQLFile introspectionSDL = GraphQLIntrospectionFilesManager.getOrCreateIntrospectionSDL(virtualFile, psiFile);
                            if (introspectionFiles.add(introspectionSDL)) {
                                // index the associated introspection SDL from a JSON introspection result file
                                introspectionSDL.accept(identifierVisitor.get());
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
            GlobalSearchScope searchScope = getResolveScope(scopedElement);

            processElementsWithWordUsingIdentifierIndex(searchScope, word, processor);

            final PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof PsiNamedElement && word.equals(((PsiNamedElement) element).getName())) {
                        if (!processor.process((PsiNamedElement) element)) {
                            return; // done processing
                        }
                    }
                    super.visitElement(element);
                }
            };

            // finally, look in the current scratch file
            PsiFile containingFile = scopedElement.getContainingFile();
            VirtualFile originalVirtualFile = GraphQLPsiUtil.getOriginalVirtualFile(containingFile);
            if (originalVirtualFile != null && GraphQLFileType.isGraphQLScratchFile(myProject, originalVirtualFile)) {
                containingFile.accept(visitor);
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

    @NotNull
    private GlobalSearchScope createExternalDefinitionsLibraryScope() {
        Collection<VirtualFile> roots = GraphQLLibraryRootsProvider.getLibraries(myProject)
            .stream()
            .flatMap(l -> l.getSourceRoots().stream())
            .collect(Collectors.toSet());
        return GlobalSearchScope.filesWithLibrariesScope(myProject, roots);
    }

    @Override
    public void dispose() {
    }
}
