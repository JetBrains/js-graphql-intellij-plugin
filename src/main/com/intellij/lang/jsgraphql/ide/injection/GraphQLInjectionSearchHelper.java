/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GraphQLInjectionSearchHelper {

    static @Nullable GraphQLInjectionSearchHelper getInstance() {
        return ApplicationManager.getApplication().getService(GraphQLInjectionSearchHelper.class);
    }

    /**
     * Gets whether the specified host is a target for GraphQL Injection
     */
    boolean isGraphQLLanguageInjectionTarget(PsiElement host);

    /**
     * Process injected GraphQL PsiFiles
     *
     * @param project       a project
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param consumer      a consumer that will be invoked for each injected GraphQL PsiFile
     */
    void processInjectedGraphQLPsiFiles(@NotNull Project project,
                                        @NotNull GlobalSearchScope schemaScope,
                                        @NotNull Processor<PsiFile> consumer);

    /**
     * Inline-replaces the use of escaped string quotes which delimit GraphQL injections, e.g. an escaped backtick '\`'
     * in JavaScript tagged template literals, such that the injected GraphQL text represents valid GraphQL
     *
     * @param rawGraphQLText the raw injected GraphQL text to escape
     * @return the text with injection-delimiting escaped while preserving text length and token positions, e.g. '\`' becomes ' `'
     * @see PsiLanguageInjectionHost#createLiteralTextEscaper()
     */
    String applyInjectionDelimitingQuotesEscape(String rawGraphQLText);
}
