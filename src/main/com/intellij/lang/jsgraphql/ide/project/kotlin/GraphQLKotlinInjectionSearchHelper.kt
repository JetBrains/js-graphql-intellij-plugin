/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.project.kotlin

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.jsgraphql.ide.injection.kotlin.GraphQLLanguageInjectionUtil
import com.intellij.lang.jsgraphql.ide.project.KotlinGraphQLInjectionSearchHelper
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLanguageInjectionHost.Shred
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import org.apache.commons.lang.StringUtils
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import java.util.function.Consumer

class GraphQLKotlinInjectionSearchHelper : KotlinGraphQLInjectionSearchHelper {
    override fun isKotlinGraphQLLanguageInjectionTarget(host: PsiElement): Boolean {
        return GraphQLLanguageInjectionUtil.isKtGraphQLLanguageInjectionTarget(host)
    }

    /**
     * Uses the [GraphQLInjectionIndex] to process injected GraphQL PsiFiles
     *
     * @param scopedElement the starting point of the enumeration settings the scopedElement of the processing
     * @param schemaScope   the search scope to use for limiting the schema definitions
     * @param consumer      a consumer that will be invoked for each injected GraphQL PsiFile
     */
    override fun processInjectedGraphQLPsiFiles(
        scopedElement: PsiElement,
        schemaScope: GlobalSearchScope,
        consumer: Consumer<PsiFile>
    ) {
        try {
            val psiManager = PsiManager.getInstance(scopedElement.project)
            val injectedLanguageManager = InjectedLanguageManager.getInstance(scopedElement.project)
            FileBasedIndex.getInstance().getFilesWithKey(
                GraphQLInjectionIndex.NAME,
                setOf(GraphQLInjectionIndex.DATA_KEY),
                { virtualFile: VirtualFile? ->
                    val fileWithInjection = psiManager.findFile(virtualFile!!)
                    fileWithInjection?.accept(object : PsiRecursiveElementVisitor() {
                        override fun visitElement(element: PsiElement) {
                            if (GraphQLLanguageInjectionUtil.isKtGraphQLLanguageInjectionTarget(element)) {
                                val text = getTextFromKtStringTemplateExpression(element as KtStringTemplateExpression)
                                print(text)

                                injectedLanguageManager.enumerate(element) { injectedPsi: PsiFile, places: List<Shred?>? ->
                                    consumer.accept(
                                        injectedPsi
                                    )
                                }
                            } else {
                                // visit deeper until injection found
                                super.visitElement(element)
                            }
                        }
                    })
                    true
                },
                schemaScope
            )
        } catch (e: IndexNotReadyException) {
            // can't search yet (e.g. during project startup)
        }
    }

    fun getTextFromKtStringTemplateExpression(element: KtStringTemplateExpression): String {

        val firstChildType = (element.firstChild as LeafPsiElement).elementType
        val lastChildType = (element.lastChild as LeafPsiElement).elementType
        if (firstChildType.toString() == "OPEN_QUOTE" && lastChildType.toString() == "CLOSING_QUOTE") {
            val prefixLength = (element.firstChild as LeafPsiElement).text.length
            val assignedValuePre = element.text.removeRange(0, prefixLength)
            return assignedValuePre.removeRange(assignedValuePre.length - prefixLength, assignedValuePre.length)
                .trimEnd()
        }
        return ""
    }

    override fun applyInjectionDelimitingQuotesEscape(rawGraphQLText: String): String {
        return if (rawGraphQLText != null && rawGraphQLText.contains("\\`")) {
            // replace escaped backticks in template literals with a whitespace and the backtick to preserve token
            // positions for error mappings etc.
            StringUtils.replace(rawGraphQLText, "\\`", " `")
        } else rawGraphQLText
    }
}
