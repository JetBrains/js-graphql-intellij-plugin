/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.javascript.injection

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectedLanguage
import com.intellij.psi.PsiElement

class GraphQLJavaScriptInjectedLanguage : GraphQLInjectedLanguage {
  override fun accepts(host: PsiElement): Boolean {
    return host is JSElement
  }

  override fun isLanguageInjectionTarget(host: PsiElement?): Boolean {
    return GraphQLJavaScriptInjectionUtils.isLanguageInjectionTarget(host)
  }

  override fun escapeHostElements(rawText: String?): String? {
    return if (rawText != null && rawText.contains("\\`")) {
      // replace escaped backticks in template literals with a whitespace and the backtick to preserve token
      // positions for error mappings etc.
      rawText.replace("\\`", " `")
    }
    else {
      rawText
    }
  }

  override fun getInjectedTextForIndexing(host: PsiElement): String? {
    if (host !is JSStringTemplateExpression) {
      return null
    }

    val fragments = GraphQLJavaScriptInjectionUtils.getInjectionPlaces(host)
    val templateText = host.text
    val result = buildString {
      for (fragment in fragments) {
        fragment.prefix?.let { append(it) }
        append(fragment.range.substring(templateText))
        fragment.suffix?.let { append(it) }
      }
    }
    return result
  }
}
