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
import com.intellij.lang.jsgraphql.GraphQLFileType;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLFragmentNameIndex;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLIdentifierIndex;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionFilesManager;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLPsiUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Enables cross-file searches for PSI references
 */
public class GraphQLPsiSearchHelper implements Disposable {

    private final Project myProject;
    private final PsiManager myPsiManager;
    private final @Nullable GraphQLInjectionSearchHelper myInjectionSearchHelper;
    private final InjectedLanguageManager myInjectedLanguageManager;

    public static GraphQLPsiSearchHelper getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, GraphQLPsiSearchHelper.class);
    }

    public GraphQLPsiSearchHelper(@NotNull final Project project) {
        myProject = project;
        myPsiManager = PsiManager.getInstance(myProject);
        myInjectionSearchHelper = GraphQLInjectionSearchHelper.getInstance();
        myInjectedLanguageManager = InjectedLanguageManager.getInstance(myProject);
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
            GlobalSearchScope schemaScope = GraphQLScopeProvider.getInstance(myProject).getResolveScope(context);
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
                            GraphQLFile introspectionSDL =
                                GraphQLIntrospectionFilesManager.getOrCreateIntrospectionSDL(virtualFile, psiFile);
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
            GlobalSearchScope searchScope = GraphQLScopeProvider.getInstance(myProject).getResolveScope(scopedElement);

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
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param processor     a processor that will be invoked for each injected GraphQL PsiFile
     */
    public void processInjectedGraphQLPsiFiles(@NotNull GlobalSearchScope schemaScope, @NotNull Processor<PsiFile> processor) {
        if (myInjectionSearchHelper != null) {
            myInjectionSearchHelper.processInjectedGraphQLPsiFiles(myProject, schemaScope, processor);
        }
    }

    @Override
    public void dispose() {
    }
}
