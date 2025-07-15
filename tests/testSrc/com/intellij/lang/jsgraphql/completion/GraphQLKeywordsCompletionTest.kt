/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion

import com.intellij.lang.jsgraphql.GraphQLCompletionTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLKeywordsCompletionTest : GraphQLCompletionTestCaseBase() {
  override fun getBasePath(): String {
    return "/completion/keywords"
  }

  fun testTopLevelKeywords() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
                       "input", "interface", "mutation", "query", "scalar",
                       "schema", "subscription", "type", "union")
    checkResult(lookupElements, "input")
  }

  fun testTopLevelKeywordsOnlyAtLineStart() = runBlockingCancellable {
    val lookupElements = doTest()
    checkDoesNotContain(lookupElements, "directive", "enum", "extend", "fragment",
                        "input", "interface", "mutation", "query", "scalar",
                        "schema", "subscription", "type", "union")
  }

  fun testTopLevelKeywordsAfterComment() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
                       "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union")
  }

  fun testExtendKeywords() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "enum", "input", "interface", "scalar", "schema", "type", "union")
  }

  fun testDirectiveKeywords() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "on", "repeatable")
    checkResult(lookupElements, "repeatable")
  }

  fun testDirectiveKeywords1() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "on")
  }

  fun testImplementsKeyword1() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "implements")
    checkResult(lookupElements, "implements")
  }

  fun testImplementsKeyword2() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "implements")
    checkResult(lookupElements, "implements")
  }

  fun testImplementsKeywordExtendType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "implements")
  }

  fun testImplementsKeywordInterface() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "implements")
  }

  fun testImplementsKeywordExtendInterface() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "implements")
  }

  fun testSchemaOperationNames() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "mutation", "subscription")
    checkResult(lookupElements, "mutation")
  }

  fun testFragmentInlineOnKeyword() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "on")
    checkResult(lookupElements, "on")
  }

  fun testFragmentInlineOnKeywordWithSpace() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "on")
    checkResult(lookupElements, "on")
  }
}
