package com.intellij.lang.jsgraphql.frameworks.apollo

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLApolloValidationTest {
  private val graphQLFixture by graphQLTestFixture("/frameworks/apollo/validation")

  @Test
  fun testLocalFields() {
    graphQLFixture.doHighlightingTest()
  }
}
