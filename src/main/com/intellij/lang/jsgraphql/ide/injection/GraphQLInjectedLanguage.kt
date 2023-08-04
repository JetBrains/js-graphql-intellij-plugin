/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.injection

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement

interface GraphQLInjectedLanguage {
  companion object {
    @JvmField
    val EP_NAME =
      ExtensionPointName.create<GraphQLInjectedLanguage>("com.intellij.lang.jsgraphql.injectedLanguage")

    @JvmStatic
    fun forElement(host: PsiElement): GraphQLInjectedLanguage? {
      return EP_NAME.findFirstSafe { it.accepts(host) }
    }
  }

  fun accepts(host: PsiElement): Boolean

  /**
   * Gets whether the specified host is a target for GraphQL Injection.
   */
  fun isLanguageInjectionTarget(host: PsiElement?): Boolean

  /**
   * Inline-replaces the use of escaped string quotes which delimit GraphQL injections, e.g. an escaped backtick '\`'.
   * in JavaScript tagged template literals, such that the injected GraphQL text represents valid GraphQL.
   *
   * @param rawText the raw injected GraphQL text to escape
   * @return the text with injection-delimiting escaped while preserving text length and token positions, e.g. '\`' becomes ' `'
   */
  fun escapeHostElements(rawText: String?): String?

  fun getInjectedTextForIndexing(host: PsiElement): String?
}
