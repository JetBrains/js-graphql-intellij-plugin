/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion

import com.intellij.lang.jsgraphql.graphQLCompletionTestFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLKeywordsCompletionTest {
  private val completionFixture by graphQLCompletionTestFixture("/completion/keywords")

  @Test
  fun testTopLevelKeywords(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
                                         "input", "interface", "mutation", "query", "scalar",
                                         "schema", "subscription", "type", "union")
    completionFixture.checkResult(lookupElements, "input")
  }

  @Test
  fun testTopLevelKeywordsOnlyAtLineStart(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkDoesNotContain(lookupElements, "directive", "enum", "extend", "fragment",
                                          "input", "interface", "mutation", "query", "scalar",
                                          "schema", "subscription", "type", "union")
  }

  @Test
  fun testTopLevelKeywordsAfterComment(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
                                         "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union")
  }

  @Test
  fun testExtendKeywords(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "enum", "input", "interface", "scalar", "schema", "type", "union")
  }

  @Test
  fun testDirectiveKeywords(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "on", "repeatable")
    completionFixture.checkResult(lookupElements, "repeatable")
  }

  @Test
  fun testDirectiveKeywords1(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "on")
  }

  @Test
  fun testImplementsKeyword1(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "implements")
    completionFixture.checkResult(lookupElements, "implements")
  }

  @Test
  fun testImplementsKeyword2(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "implements")
    completionFixture.checkResult(lookupElements, "implements")
  }

  @Test
  fun testImplementsKeywordExtendType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "implements")
  }

  @Test
  fun testImplementsKeywordInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "implements")
  }

  @Test
  fun testImplementsKeywordExtendInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "implements")
  }

  @Test
  fun testSchemaOperationNames(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "mutation", "subscription")
    completionFixture.checkResult(lookupElements, "mutation")
  }

  @Test
  fun testFragmentInlineOnKeyword(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "on")
    completionFixture.checkResult(lookupElements, "on")
  }

  @Test
  fun testFragmentInlineOnKeywordWithSpace(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "on")
    completionFixture.checkResult(lookupElements, "on")
  }
}
