/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.javascript;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.jsgraphql.ide.injection.javascript.GraphQLLanguageInjectionUtil;
import com.intellij.lang.jsgraphql.ide.project.GraphQLInjectionSearchHelper;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.function.Consumer;

public class GraphQLJavascriptInjectionSearchHelper implements GraphQLInjectionSearchHelper {

    @Override
    public boolean isJSGraphQLLanguageInjectionTarget(PsiElement host) {
        return GraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(host);
    }

    /**
     * Uses the {@link GraphQLInjectionIndex} to process injected GraphQL PsiFiles
     *
     * @param scopedElement the starting point of the enumeration settings the scopedElement of the processing
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param consumer      a consumer that will be invoked for each injected GraphQL PsiFile
     */
    public void processInjectedGraphQLPsiFiles(PsiElement scopedElement, GlobalSearchScope schemaScope, Consumer<PsiFile> consumer) {
        try {
            final PsiManager psiManager = PsiManager.getInstance(scopedElement.getProject());
            final InjectedLanguageManager injectedLanguageManager = InjectedLanguageManager.getInstance(scopedElement.getProject());
            FileBasedIndex.getInstance().getFilesWithKey(GraphQLInjectionIndex.NAME, Collections.singleton(GraphQLInjectionIndex.DATA_KEY), virtualFile -> {
                final PsiFile fileWithInjection = psiManager.findFile(virtualFile);
                if (fileWithInjection != null) {
                    fileWithInjection.accept(new PsiRecursiveElementVisitor() {
                        @Override
                        public void visitElement(PsiElement element) {
                            if (GraphQLLanguageInjectionUtil.isJSGraphQLLanguageInjectionTarget(element)) {
                                injectedLanguageManager.enumerate(element, (injectedPsi, places) -> {
                                    consumer.accept(injectedPsi);
                                });
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
