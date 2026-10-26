package com.intellij.lang.jsgraphql.highlighting

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLHighlightingTest {
  private val graphQLFixture by graphQLTestFixture("/highlighting")

  @Test
  fun testOperationDefinitions() {
    doTest()
  }

  @Test
  fun testFragmentDefinition() {
    doTest()
  }

  @Test
  fun testFragmentSpread() {
    doTest()
  }

  @Test
  fun testTypeNameDefinitions() {
    doTest()
  }

  @Test
  fun testFields() {
    doTest()
  }

  @Test
  fun testFieldDefinitions() {
    doTest()
  }

  @Test
  fun testInputFieldDefinitions() {
    doTest()
  }

  @Test
  fun testArgumentDefinitions() {
    doTest()
  }

  @Test
  fun testArguments() {
    doTest()
  }

  @Test
  fun testVariables() {
    doTest()
  }

  @Test
  fun testValues() {
    doTest()
  }

  @Test
  fun testDirectives() {
    doTest()
  }

  @Test
  fun testKeywordsAsIdentifiers() {
    doTest()
  }

  @Test
  fun testSchemaDefinition() {
    doTest()
  }

  private fun doTest() {
    val highlightTestInfo = graphQLFixture.fixture.testFile(graphQLFixture.getTestName(false) + ".graphql")
    highlightTestInfo.checkSymbolNames()
    highlightTestInfo.test()
  }
}
