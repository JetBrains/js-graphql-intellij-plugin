package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLJavaScriptInjectionHighlightingTest {
  private val graphQLFixture by graphQLTestFixture("/injection/highlighting/js")

  @Test
  fun testArgument() {
    doTest()
  }

  private fun doTest() {
    val highlightTestInfo = graphQLFixture.fixture.testFile(graphQLFixture.getTestName(true) + ".js")
    highlightTestInfo.checkSymbolNames()
    highlightTestInfo.test()
  }
}
