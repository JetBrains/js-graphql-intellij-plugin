/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.indexing.javascript;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionSearchHelper;
import com.intellij.lang.jsgraphql.ide.injection.javascript.GraphQLLanguageInjectionUtil;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GraphQLJavaScriptInjectionSearchHelper implements GraphQLInjectionSearchHelper {

    @Override
    public boolean isGraphQLLanguageInjectionTarget(PsiElement host) {
        return GraphQLLanguageInjectionUtil.isGraphQLLanguageInjectionTarget(host);
    }

    /**
     * Uses the {@link GraphQLInjectionIndex} to process injected GraphQL PsiFiles
     *
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param processor     a processor that will be invoked for each injected GraphQL PsiFile
     */
    public void processInjectedGraphQLPsiFiles(@NotNull Project project,
                                               @NotNull GlobalSearchScope schemaScope,
                                               @NotNull Processor<? super PsiFile> processor) {
        try {
            final PsiManager psiManager = PsiManager.getInstance(project);
            final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(project);
            FileBasedIndex.getInstance().getFilesWithKey(GraphQLInjectionIndex.NAME, Collections.singleton(GraphQLInjectionIndex.DATA_KEY),
                virtualFile -> {
                    final PsiFile fileWithInjection = psiManager.findFile(virtualFile);
                    if (fileWithInjection != null) {
                        fileWithInjection.accept(new PsiRecursiveElementVisitor() {
                            @Override
                            public void visitElement(@NotNull PsiElement element) {
                                if (GraphQLLanguageInjectionUtil.isGraphQLLanguageInjectionTarget(element)) {
                                    injectedLanguageManager.enumerate(element, (injectedPsi, places) -> processor.process(injectedPsi));
                                } else {
                                    // visit deeper until injection found
                                    super.visitElement(element);
                                }
                            }
                        });
                    }
                    return true;
                }, schemaScope);
        } catch (IndexNotReadyException e) {
            // can't search yet (e.g. during project startup)
        }
    }

    @Override
    public String applyInjectionDelimitingQuotesEscape(String rawGraphQLText) {
        if (rawGraphQLText != null && rawGraphQLText.contains("\\`")) {
            // replace escaped backticks in template literals with a whitespace and the backtick to preserve token
            // positions for error mappings etc.
            return StringUtils.replace(rawGraphQLText, "\\`", " `");
        }
        return rawGraphQLText;
    }
}
