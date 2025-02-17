package com.intellij.lang.jsgraphql.ide.injection

import com.intellij.lang.Language
import com.intellij.lang.jsgraphql.GraphQLLanguage
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil

object GraphQLInjectionUtils {
  internal val GRAPHQL_INJECTION_KEY: Key<Boolean> = Key.create("graphql.injection")

  internal const val GRAPHQL_EXTERNAL_FRAGMENT: String = "EXTERNAL_FRAGMENT"

  @JvmStatic
  fun registerInjection(host: PsiLanguageInjectionHost, language: Language) {
    InjectedLanguageUtil.getCachedInjectedFileWithLanguage(host, language)?.putUserData(GRAPHQL_INJECTION_KEY, true)
  }

  @JvmStatic
  fun isInjectedElement(element: PsiElement): Boolean = GRAPHQL_INJECTION_KEY.get(element.containingFile, false)

  @JvmStatic
  fun isTemplatePlaceholder(element: PsiElement): Boolean = element.text.startsWith(GRAPHQL_EXTERNAL_FRAGMENT) // TODO: improve

  @JvmStatic
  fun createPlaceholderName(index: Int): String = "${GRAPHQL_EXTERNAL_FRAGMENT}_$index"

  /**
   * Should be used during indexing since [com.intellij.lang.injection.InjectedLanguageManager.enumerateEx]
   * doesn't work consistently with non-physical PSI.
   * In all other cases [com.intellij.lang.injection.InjectedLanguageManager] must be used instead.
   *
   * @return `true` if an injection is expected in the current context, no matter if it's correctly injected or not.
   */
  @JvmStatic
  fun visitInjectionAsRawText(host: PsiLanguageInjectionHost, visitor: PsiElementVisitor): Boolean {
    val injectedLanguage = GraphQLInjectedLanguage.forElement(host)
    if (injectedLanguage != null && injectedLanguage.isLanguageInjectionTarget(host)) {
      val injectedText = injectedLanguage.getInjectedTextForIndexing(host)
      if (injectedText != null) {
        val psiFileFactory = PsiFileFactory.getInstance(host.project)
        val injectedFile = psiFileFactory
          .createFileFromText("injected.graphql", GraphQLLanguage.INSTANCE, injectedText, false, true)
        injectedFile.accept(visitor)
      }
      return true
    }
    return false
  }
}