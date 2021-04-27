package com.intellij.lang.jsgraphql.ide.project

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import java.util.function.Consumer

interface KotlinGraphQLInjectionSearchHelper {
    /**
     * Gets whether the specified host is a target for GraphQL Injection
     */
    fun isKotlinGraphQLLanguageInjectionTarget(host: PsiElement): Boolean

    /**
     * Process injected GraphQL PsiFiles
     *
     * @param scopedElement the starting point of the enumeration settings the scopedElement of the processing
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param consumer      a consumer that will be invoked for each injected GraphQL PsiFile
     */
    fun processInjectedGraphQLPsiFiles(
        scopedElement: PsiElement,
        schemaScope: GlobalSearchScope,
        consumer: Consumer<PsiFile>
    )

    /**
     * Inline-replaces the use of escaped string quotes which delimit GraphQL injections, e.g. an escaped backtick '\`'
     * in JavaScript tagged template literals, such that the injected GraphQL text represents valid GraphQL
     * @param rawGraphQLText the raw injected GraphQL text to escape
     * @return the text with injection-delimiting escaped while preserving text length and token positions, e.g. '\`' becomes ' `'
     * @see PsiLanguageInjectionHost.createLiteralTextEscaper
     */
    fun applyInjectionDelimitingQuotesEscape(rawGraphQLText: String): String
}
