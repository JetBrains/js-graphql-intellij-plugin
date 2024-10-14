/*
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.search;


import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLFragmentNameIndex;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLIdentifierIndex;
import com.intellij.lang.jsgraphql.ide.indexing.GraphQLInjectionIndex;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage;
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider;
import com.intellij.lang.jsgraphql.psi.GraphQLFile;
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Enables cross-file searches for PSI references
 */
public class GraphQLPsiSearchHelper implements Disposable {

  private static final Logger LOG = Logger.getInstance(GraphQLPsiSearchHelper.class);

  private final Project myProject;
  private final PsiManager myPsiManager;

  public static GraphQLPsiSearchHelper getInstance(@NotNull Project project) {
    return project.getService(GraphQLPsiSearchHelper.class);
  }

  public GraphQLPsiSearchHelper(@NotNull Project project) {
    myProject = project;
    myPsiManager = PsiManager.getInstance(myProject);
  }

  /**
   * Finds all fragment definitions inside the scope of the specified element
   *
   * @param context the starting point for finding known fragment definitions
   * @return a list of known fragment definitions, or an empty list if the index is not yet ready
   */
  public @NotNull List<GraphQLFragmentDefinition> findFragmentDefinitions(@NotNull PsiElement context) {
    if (DumbService.isDumb(context.getProject())) return Collections.emptyList();

    try {
      List<GraphQLFragmentDefinition> fragmentDefinitions = new ArrayList<>();
      GlobalSearchScope scope = GraphQLScopeProvider.getInstance(myProject).getResolveScope(context, false);
      PsiManager psiManager = PsiManager.getInstance(myProject);
      FileBasedIndex.getInstance().processFilesContainingAllKeys(
        GraphQLFragmentNameIndex.NAME,
        Collections.singleton(GraphQLFragmentNameIndex.HAS_FRAGMENTS),
        scope,
        null,
        virtualFile -> {
          PsiFile psiFile = psiManager.findFile(virtualFile);
          if (psiFile != null) {
            fragmentDefinitions.addAll(collectFragmentDefinitions(psiFile));
          }
          return true; // process all known fragments
        });

      return fragmentDefinitions;
    }
    catch (IndexNotReadyException e) {
      LOG.warn(e);
    }
    return Collections.emptyList();
  }

  private static @NotNull Collection<GraphQLFragmentDefinition> collectFragmentDefinitions(@NotNull PsiFile file) {
    return CachedValuesManager.getCachedValue(file, () -> {
      List<GraphQLFragmentDefinition> fragmentDefinitions = collectGraphQLFilesIncludingInjections(file).stream()
        .flatMap(graphQLFile -> graphQLFile.getDefinitions().stream())
        .filter(GraphQLFragmentDefinition.class::isInstance)
        .map(GraphQLFragmentDefinition.class::cast)
        .toList();
      return CachedValueProvider.Result.create(fragmentDefinitions, file);
    });
  }

  private static @NotNull Collection<GraphQLFile> collectGraphQLFilesIncludingInjections(@NotNull PsiFile file) {
    if (file instanceof GraphQLFile graphQLFile) {
      return Collections.singletonList(graphQLFile);
    }
    else {
      Set<GraphQLFile> files = new HashSet<>();
      InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(file.getProject());
      for (PsiLanguageInjectionHost host : findLanguageInjectionHosts(file)) {
        injectedLanguageManager.enumerateEx(
          host, file, false, (injectedPsi, places) -> {
            if (injectedPsi instanceof GraphQLFile graphQLFile) {
              files.add(graphQLFile);
            }
          }
        );
      }
      return files;
    }
  }

  private static @NotNull Collection<PsiLanguageInjectionHost> findLanguageInjectionHosts(@NotNull PsiFile file) {
    return CachedValuesManager.getCachedValue(file, () -> {
      List<PsiLanguageInjectionHost> injectionHosts =
        PsiTreeUtil.collectElementsOfType(file, PsiLanguageInjectionHost.class).stream()
          .filter(element -> {
            GraphQLInjectedLanguage injectedLanguage = GraphQLInjectedLanguage.forElement(element);
            return injectedLanguage != null && injectedLanguage.isLanguageInjectionTarget(element);
          })
          .toList();
      return CachedValueProvider.Result.create(injectionHosts, file);
    });
  }

  public void processNamedElements(@NotNull PsiElement context,
                                   @NotNull String name,
                                   @NotNull Processor<? super PsiNamedElement> processor) {
    GlobalSearchScope scope = GraphQLScopeProvider.getInstance(context.getProject()).getResolveScope(context);
    processNamedElements(context.getProject(), name, scope, processor);
  }

  /**
   * Processes all named elements that match the specified name, e.g. the declaration of a type name
   */
  public void processNamedElements(@NotNull Project project,
                                   @NotNull String name,
                                   @NotNull GlobalSearchScope scope,
                                   @NotNull Processor<? super PsiNamedElement> processor) {
    if (DumbService.isDumb(project)) return;

    try {
      FileBasedIndex.getInstance().getFilesWithKey(GraphQLIdentifierIndex.NAME, Collections.singleton(name), virtualFile -> {
        PsiFile psiFile = myPsiManager.findFile(virtualFile);
        if (psiFile != null) {
          for (GraphQLFile graphQLFile : collectGraphQLFilesIncludingInjections(psiFile)) {
            MultiMap<String, PsiNamedElement> namedElements = graphQLFile.getNamedElements();
            for (PsiNamedElement namedElement : namedElements.get(name)) {
              if (!processor.process(namedElement)) return false;
            }
          }
        }
        return true;
      }, scope);
    }
    catch (IndexNotReadyException e) {
      LOG.warn(e);
    }
  }

  /**
   * Process injected GraphQL files
   *
   * @param scope     the search scope to use for limiting the schema definitions
   * @param processor a processor that will be invoked for each injected GraphQL file
   */
  public void processInjectedGraphQLFiles(@NotNull Project project,
                                          @NotNull GlobalSearchScope scope,
                                          @NotNull Processor<? super GraphQLFile> processor) {
    if (DumbService.isDumb(project)) return;

    try {
      PsiManager psiManager = PsiManager.getInstance(myProject);
      FileBasedIndex.getInstance().getFilesWithKey(
        GraphQLInjectionIndex.NAME,
        Collections.singleton(GraphQLInjectionIndex.INJECTION_MARKER),
        virtualFile -> {
          ProgressManager.checkCanceled();
          PsiFile psiFile = psiManager.findFile(virtualFile);
          if (psiFile != null && !(psiFile instanceof GraphQLFile)) {
            for (GraphQLFile graphQLFile : collectGraphQLFilesIncludingInjections(psiFile)) {
              if (!processor.process(graphQLFile)) return false;
            }
          }
          return true;
        },
        scope
      );
    }
    catch (IndexNotReadyException e) {
      LOG.warn(e);
    }
  }

  @Override
  public void dispose() {
  }
}
