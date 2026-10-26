package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLSuppressTest {
  private val graphQLFixture by graphQLTestFixture("/validation/suppress", enableAllInspections = true)

  @Test
  fun testSuppressOnDefinition() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testSuppressOnFile() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testSuppressOnStatementInjected() {
    graphQLFixture.doHighlightingTest("js")
  }

  @Test
  fun testSuppressOnFileInjected() {
    graphQLFixture.doHighlightingTest("js")
  }
}
