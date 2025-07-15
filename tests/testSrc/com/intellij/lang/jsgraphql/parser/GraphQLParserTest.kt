/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.parser

import com.intellij.lang.jsgraphql.GraphQLParserDefinition
import com.intellij.lang.jsgraphql.getTestDataPath
import com.intellij.testFramework.ParsingTestCase

class GraphQLParserTest : ParsingTestCase("", "graphql", GraphQLParserDefinition()) {
  fun testParsingTestData() {
    doTest(true)
  }

  fun testDirectives() {
    doTest(true, true)
  }

  fun testSchemaDescriptions() {
    doTest(true, true)
  }

  fun testSingleLineDescriptions() {
    doTest(true, true)
  }

  fun testMultilineDescriptions() {
    doTest(true, true)
  }

  fun testExtendSchema() {
    doTest(true, true)
  }

  fun testEmoji() {
    doTest(true, true)
  }

  fun testKeywordsAsIdentifiers() {
    doTest(true, true)
  }

  fun testDescriptionsForExecutableDefinitions() {
    doTest(true, true)
  }

  override fun getTestDataPath(): String {
    return getTestDataPath("parser")
  }

  override fun includeRanges(): Boolean {
    return true
  }
}
