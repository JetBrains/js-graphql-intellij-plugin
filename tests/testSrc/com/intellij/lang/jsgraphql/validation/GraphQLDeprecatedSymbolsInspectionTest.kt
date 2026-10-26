package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLDeprecatedSymbolsInspectionTest {
  private val graphQLFixture by graphQLTestFixture("/validation/deprecated", enableAllInspections = true)

  @Test
  fun testDeprecatedSymbols() {
    graphQLFixture.doHighlightingTest()
  }
}