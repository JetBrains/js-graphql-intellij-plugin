package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLOperationsValidationTest {
  private val graphQLFixture by graphQLTestFixture("/validation/operations", enableAllInspections = true)

  @Test
  fun testRepeatableDirectives() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testWrongTypes() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testDuplicates() {
    graphQLFixture.doHighlightingTest()
  }
}
