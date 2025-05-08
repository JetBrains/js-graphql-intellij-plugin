/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.graphql.javascript.injection

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.patterns.JSPatterns
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.ecma6.ES6TaggedTemplateExpression
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.lang.javascript.psi.ecma6.TypeScriptAsExpression
import com.intellij.lang.jsgraphql.ide.injection.GraphQLCommentBasedInjectionHelper
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionFragment
import com.intellij.lang.jsgraphql.ide.injection.GraphQLInjectionUtils
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.text.CharArrayUtil
import java.util.regex.Pattern

object GraphQLJavaScriptInjectionUtils {
  private const val GRAPHQL_EOL_COMMENT = "#graphql"

  // Min length for injection - `#graphql`
  private const val GRAPHQL_EOL_COMMENT_MIN_TEMPLATE_LENGTH = 10

  private val GRAPHQL_C_STYLE_COMMENT_PATTERN = Pattern.compile("/\\*\\s*GraphQL\\s*\\*/")

  private const val RELAY_QL_TEMPLATE_TAG: String = "Relay.QL"
  private const val GRAPHQL_TEMPLATE_TAG: String = "graphql"
  private const val GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG: String = "graphql.experimental"
  private const val GQL_TEMPLATE_TAG: String = "gql"
  private const val APOLLO_GQL_TEMPLATE_TAG: String = "Apollo.gql"

  private val GRAPHQL_CALL_ARG_PATTERN = JSPatterns.jsArgument(GRAPHQL_TEMPLATE_TAG, 0)
  private val GQL_CALL_ARG_PATTERN = JSPatterns.jsArgument(GQL_TEMPLATE_TAG, 0)

  private val SUPPORTED_TAG_NAMES = setOf(
    RELAY_QL_TEMPLATE_TAG,
    GRAPHQL_TEMPLATE_TAG,
    GRAPHQL_EXPERIMENTAL_TEMPLATE_TAG,
    GQL_TEMPLATE_TAG,
    APOLLO_GQL_TEMPLATE_TAG
  )

  @JvmStatic
  fun isLanguageInjectionTarget(host: PsiElement?): Boolean {
    if (host !is JSStringTemplateExpression) {
      return false
    }

    // gql``, Relay.QL``, etc
    if (isInjectedUsingTemplateTag(host)) {
      return true
    }

    if (isInjectedInCallArgument(host)) {
      return true
    }

    // built-in "language=GraphQL" injection comments
    if (isInjectedUsingBuiltInComments(host)) {
      return true
    }

    // /* GraphQL */
    if (isInjectedUsingCStyleComment(host)) {
      return true
    }

    // # graphql
    if (isInjectedUsingCommentInside(host.text)) {
      return true
    }

    return false
  }

  private fun isInjectedUsingBuiltInComments(host: PsiElement): Boolean {
    val commentBasedInjectionHelper = GraphQLCommentBasedInjectionHelper.getInstance()
    return commentBasedInjectionHelper != null && commentBasedInjectionHelper.isGraphQLInjectedUsingComment(host)
  }

  private fun isInjectedUsingTemplateTag(template: JSStringTemplateExpression): Boolean {
    val parent = template.parent
    if (parent !is ES6TaggedTemplateExpression) return false

    // check if we're a graphql tagged template
    val tagExpression = PsiTreeUtil.getPrevSiblingOfType(template, JSReferenceExpression::class.java)
    if (tagExpression == null) {
      return false
    }

    val tagText = tagExpression.text
    if (SUPPORTED_TAG_NAMES.contains(tagText)) {
      return true
    }

    val builderTailName = tagExpression.referenceName
    // a builder pattern that ends in a tagged template, e.g. someQueryAPI.graphql``
    return builderTailName != null && SUPPORTED_TAG_NAMES.contains(builderTailName)
  }

  private fun isInjectedInCallArgument(template: JSStringTemplateExpression): Boolean {
    val parent = template.parent
    val expr = parent as? ES6TaggedTemplateExpression ?: template
    return GRAPHQL_CALL_ARG_PATTERN.accepts(expr) || GQL_CALL_ARG_PATTERN.accepts(expr)
  }

  /**
   * Checks a case when the possible injection contains a EOL comment "# graphql".
   */
  private fun isInjectedUsingCommentInside(text: String): Boolean {
    val length = text.length
    if (length < GRAPHQL_EOL_COMMENT_MIN_TEMPLATE_LENGTH) return false

    var offset = 0
    if (!StringUtil.isChar(text, offset++, '`')) return false
    offset = CharArrayUtil.shiftForward(text, offset, " \n")
    if (offset >= length) return false
    return text.startsWith(GRAPHQL_EOL_COMMENT, offset)
  }

  private fun isInjectedUsingCStyleComment(template: JSStringTemplateExpression): Boolean {
    var anchor: PsiElement = template

    if (anchor.getParent() is ES6TaggedTemplateExpression) {
      anchor = anchor.parent
    }
    if (anchor.getParent() is TypeScriptAsExpression) {
      anchor = anchor.parent
    }

    val element = PsiTreeUtil.skipWhitespacesBackward(anchor)
    if (element == null || element.elementType !== JSTokenTypes.C_STYLE_COMMENT) {
      return false
    }

    return GRAPHQL_C_STYLE_COMMENT_PATTERN.matcher(element.text).matches()
  }

  internal fun getInjectionPlaces(context: JSStringTemplateExpression): List<GraphQLInjectionFragment> {
    val ranges = context.stringRangesWithEmpty
    val arguments = context.arguments

    // `${}` and ``
    if (ranges.size <= 2 && ranges.all { it.isEmpty }) {
      return emptyList()
    }

    return ranges.mapIndexed { i, textRange ->
      GraphQLInjectionFragment(null, getArgumentPlaceholder(arguments.elementAtOrNull(i), i), textRange)
    }
  }

  private fun getArgumentPlaceholder(argument: JSExpression?, index: Int): String? {
    if (argument == null) return null

    if (argument is JSLiteralExpression) {
      val value = argument.value
      if (value != null) {
        return value.toString()
      }
    }

    return GraphQLInjectionUtils.createPlaceholderName(index)
  }
}
