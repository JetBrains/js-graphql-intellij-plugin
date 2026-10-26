package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLJavaScriptTemplatesSyntaxTest {
  private val graphQLFixture by graphQLTestFixture("/injection/templates/js", enableAllInspections = true)

  @Test
  fun testTypedOperationDirectives() {
    doTest()
  }

  @Test
  fun testFieldArg() {
    doTest()
  }

  @Test
  fun testSelectionSet() {
    doTest()
  }

  @Test
  fun testOperationDefinitions() {
    doTest()
  }

  @Test
  fun testTypeDefinitions() {
    doTest(false)
  }

  @Test
  fun testFieldDefinitions() {
    doTest(false)
  }

  @Test
  fun testFieldDirectives() {
    doTest(false)
  }

  @Test
  fun testTypeDefinitionDirectives() {
    doTest(false)
  }

  @Test
  fun testFragmentDirectives() {
    doTest()
  }

  @Test
  fun testVariableDefinitions() {
    doTest()
  }

  @Test
  fun testEnumFieldsExpression() {
    doTest(false)
  }

  @Test
  fun testOperationName() {
    doTest()
  }

  @Test
  fun testDirectiveArgument() {
    doTest(false)
  }

  private fun doTest(withSchema: Boolean = true) {
    if (withSchema) {
      graphQLFixture.fixture.copyFileToProject("${graphQLFixture.getTestName(true)}.graphql")
    }
    graphQLFixture.fixture.configureByFile("${graphQLFixture.getTestName(true)}.js")
    graphQLFixture.fixture.checkHighlighting()
  }
}