/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.search;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLFragmentNameIndex;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLIdentifierIndex;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLInjectionIndex;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLFileMappingManager;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Enables cross-file searches for PSI references
 */
public class GraphQLPsiSearchHelper implements Disposable {

    private final Project myProject;
    private final PsiManager myPsiManager;
    private final InjectedLanguageManager myInjectedLanguageManager;

    public static GraphQLPsiSearchHelper getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPsiSearchHelper.class);
    }

    public GraphQLPsiSearchHelper(@NotNull Project project) {
        myProject = project;
        myPsiManager = PsiManager.getInstance(myProject);
        myInjectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
    }

    /**
     * Finds all fragment definitions inside the scope of the specified element
     *
     * @param context the starting point for finding known fragment definitions
     * @return a list of known fragment definitions, or an empty list if the index is not yet ready
     */
    @NotNull
    public List<GraphQLFragmentDefinition> findFragmentDefinitions(@NotNull PsiElement context) {
        try {
            final List<GraphQLFragmentDefinition> fragmentDefinitions = Lists.newArrayList();
            GlobalSearchScope scope = GraphQLScopeProvider.getInstance(myProject).getResolveScope(context, false);
            final PsiManager psiManager = PsiManager.getInstance(myProject);
            FileBasedIndex.getInstance().processFilesContainingAllKeys(
                GraphQLFragmentNameIndex.NAME,
                Collections.singleton(GraphQLFragmentNameIndex.HAS_FRAGMENTS),
                scope,
                null,
                virtualFile -> {
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
                                    if (visitLanguageInjectionHost((PsiLanguageInjectionHost) element, identifierVisitor.get())) {
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
                                               @NotNull PsiRecursiveElementVisitor visitor) {
        GraphQLInjectedLanguage injectedLanguage = GraphQLInjectedLanguage.forElement(element);
        if (injectedLanguage != null && injectedLanguage.isLanguageInjectionTarget(element)) {
            myInjectedLanguageManager.enumerateEx(
                element, element.getContainingFile(), false,
                (injectedPsi, places) -> injectedPsi.accept(visitor)
            );
            return true;
        }
        return false;
    }

    public void processNamedElements(@NotNull PsiElement context,
                                     @NotNull String name,
                                     @NotNull Processor<? super PsiNamedElement> processor) {
        GlobalSearchScope scope = GraphQLScopeProvider.getInstance(context.getProject()).getResolveScope(context);
        processNamedElements(context, name, scope, processor);
    }

    /**
     * Processes all named elements that match the specified name, e.g. the declaration of a type name
     */
    public void processNamedElements(@NotNull PsiElement context,
                                     @NotNull String name,
                                     @NotNull GlobalSearchScope scope,
                                     @NotNull Processor<? super PsiNamedElement> processor) {
        try {
            processNamedElementsUsingIdentifierIndex(scope, name, processor);
        } catch (IndexNotReadyException e) {
            // TODO: rethrow
            // can't search yet (e.g. during project startup)
        }
    }

    /**
     * Processes GraphQL named elements whose name matches the specified name within the given schema scope.
     *
     * @param scope     the schema scope which limits the processing
     * @param name      the name to match elements for
     * @param processor processor called for all GraphQL elements whose name match the specified name
     * @see GraphQLIdentifierIndex
     */
    private void processNamedElementsUsingIdentifierIndex(@NotNull GlobalSearchScope scope,
                                                          @NotNull String name,
                                                          @NotNull Processor<? super PsiNamedElement> processor) {
        FileBasedIndex.getInstance().getFilesWithKey(GraphQLIdentifierIndex.NAME, Collections.singleton(name), virtualFile -> {
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
                            final String candidate = ((PsiNamedElement) element).getName();
                            if (name.equals(candidate)) {
                                continueProcessing.set(processor.process((PsiNamedElement) element));
                            }
                        } else if (element instanceof JsonStringLiteral) {
                            GraphQLFile introspectionSDL =
                                GraphQLFileMappingManager.getInstance(myProject).getOrCreateIntrospectionSDL(virtualFile);
                            if (introspectionSDL != null && introspectionFiles.add(introspectionSDL)) {
                                // index the associated introspection SDL from a JSON introspection result file
                                introspectionSDL.accept(identifierVisitor.get());
                            }
                            return; // no need to visit deeper
                        } else if (element instanceof PsiLanguageInjectionHost) {
                            if (visitLanguageInjectionHost((PsiLanguageInjectionHost) element, identifierVisitor.get())) {
                                return;
                            }
                        }

                        super.visitElement(element);
                    }
                });

                psiFile.accept(identifierVisitor.get());
            }
            return continueProcessing.get();
        }, scope);
    }

    /**
     * Process injected GraphQL files
     *
     * @param scope     the search scope to use for limiting the schema definitions
     * @param processor a processor that will be invoked for each injected GraphQL file
     */
    public void processInjectedGraphQLFiles(@NotNull GlobalSearchScope scope, @NotNull Processor<? super PsiFile> processor) {
        try {
            final PsiManager psiManager = PsiManager.getInstance(myProject);
            final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
            FileBasedIndex.getInstance().getFilesWithKey(
                GraphQLInjectionIndex.NAME,
                Collections.singleton(GraphQLInjectionIndex.DATA_KEY),
                virtualFile -> {
                    final PsiFile psiFile = psiManager.findFile(virtualFile);
                    if (psiFile == null) {
                        return true;
                    }
                    psiFile.accept(new PsiRecursiveElementVisitor() {
                        @Override
                        public void visitElement(@NotNull PsiElement element) {
                            GraphQLInjectedLanguage injectedLanguage = GraphQLInjectedLanguage.forElement(element);
                            if (injectedLanguage != null && injectedLanguage.isLanguageInjectionTarget(element)) {
                                injectedLanguageManager.enumerate(element, (injectedPsi, places) -> processor.process(injectedPsi));
                            } else {
                                // visit deeper until injection found
                                super.visitElement(element);
                            }
                        }
                    });
                    return true;
                },
                scope
            );
        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
    }

    @Override
    public void dispose() {
    }
}
