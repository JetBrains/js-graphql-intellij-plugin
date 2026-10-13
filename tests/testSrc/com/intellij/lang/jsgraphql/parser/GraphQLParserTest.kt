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
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLParserTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val parsing by parsingFixture(projectFixture, getTestDataPath("parser"), "graphql", definitions = arrayOf(GraphQLParserDefinition()))

  @BeforeEach
  fun setUp() {
    parsing.includeRanges = true
  }

  @Test
  fun testParsingTestData() {
    parsing.doTest(true)
  }

  @Test
  fun testDirectives() {
    parsing.doTest(true, true)
  }

  @Test
  fun testSchemaDescriptions() {
    parsing.doTest(true, true)
  }

  @Test
  fun testSingleLineDescriptions() {
    parsing.doTest(true, true)
  }

  @Test
  fun testMultilineDescriptions() {
    parsing.doTest(true, true)
  }

  @Test
  fun testExtendSchema() {
    parsing.doTest(true, true)
  }

  @Test
  fun testEmoji() {
    parsing.doTest(true, true)
  }

  @Test
  fun testKeywordsAsIdentifiers() {
    parsing.doTest(true, true)
  }

  @Test
  fun testDescriptionsForExecutableDefinitions() {
    parsing.doTest(true, true)
  }
}
