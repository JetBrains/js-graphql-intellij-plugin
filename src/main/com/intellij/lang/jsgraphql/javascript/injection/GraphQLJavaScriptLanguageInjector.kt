package com.intellij.lang.jsgraphql.javascript.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.javascript.injections.JSFormattableInjectionUtil
import com.intellij.lang.javascript.injections.StringInterpolationErrorFilter
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.jsgraphql.GraphQLLanguage
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionUtils
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiElement

private val INJECTED_ELEMENTS = listOf(JSStringTemplateExpression::class.java)

class GraphQLJavaScriptLanguageInjector : MultiHostInjector {

  override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
    if (context !is JSStringTemplateExpression) {
      return
    }

    if (!GraphQLJavaScriptInjectionUtils.isLanguageInjectionTarget(context)) {
      return
    }

    val fragments = GraphQLJavaScriptInjectionUtils.getInjectionPlaces(context)
    if (fragments.isEmpty()) return

    val injectionLanguage = GraphQLLanguage.INSTANCE

    registrar.startInjecting(injectionLanguage)
    for (fragment in fragments) {
      registrar.addPlace(fragment.prefix, fragment.suffix, context, fragment.range)
    }
    registrar.doneInjecting()

    if (fragments.size > 1) {
      StringInterpolationErrorFilter.register(context, injectionLanguage)
      GraphQLInjectionUtils.registerInjection(context, injectionLanguage)
    }

    if (Registry.`is`("graphql.reformat.injections.javascript")) {
      JSFormattableInjectionUtil.setReformattableInjection(context, injectionLanguage)
    }
  }

  override fun elementsToInjectIn(): List<Class<out PsiElement>> = INJECTED_ELEMENTS
}
