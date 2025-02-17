package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase

class GraphQLJavaScriptTemplatesSyntaxTest : GraphQLTestCaseBase() {
  protected override fun getBasePath(): String {
    return "/injection/templates/js"
  }

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testTypedOperationDirectives() {
    doTest()
  }

  fun testFieldArg() {
    doTest()
  }

  // TODO: [panimaskin] fix test
  //fun testSelectionSet() {
  //  doTest()
  //}

  fun testOperationDefinitions() {
    doTest()
  }

  fun testTypeDefinitions() {
    doTest(false)
  }

  fun testFieldDefinitions() {
    doTest(false)
  }

  fun testFieldDirectives() {
    doTest(false)
  }

  fun testTypeDefinitionDirectives() {
    doTest(false)
  }

  fun testFragmentDirectives() {
    doTest()
  }

  fun testVariableDefinitions() {
    doTest()
  }

  fun testEnumFieldsExpression() {
    doTest(false)
  }

  fun testOperationName() {
    doTest()
  }

  fun testDirectiveArgument() {
    doTest(false)
  }

  private fun doTest(withSchema: Boolean = true) {
    if (withSchema) {
      myFixture.copyFileToProject("${getTestName(true)}.graphql")
    }
    myFixture.configureByFile("${getTestName(true)}.js")
    myFixture.checkHighlighting()
  }
}