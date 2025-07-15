package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.runBlockingCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphQLJavaScriptTemplatesSyntaxTest : GraphQLTestCaseBase() {
  protected override fun getBasePath(): String {
    return "/injection/templates/js"
  }

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testTypedOperationDirectives() = runBlockingCancellable {
    doTest()
  }

  fun testFieldArg() = runBlockingCancellable {
    doTest()
  }

  fun testSelectionSet() = runBlockingCancellable {
    doTest()
  }

  fun testOperationDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testTypeDefinitions() = runBlockingCancellable {
    doTest(false)
  }

  fun testFieldDefinitions() = runBlockingCancellable {
    doTest(false)
  }

  fun testFieldDirectives() = runBlockingCancellable {
    doTest(false)
  }

  fun testTypeDefinitionDirectives() = runBlockingCancellable {
    doTest(false)
  }

  fun testFragmentDirectives() = runBlockingCancellable {
    doTest()
  }

  fun testVariableDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testEnumFieldsExpression() = runBlockingCancellable {
    doTest(false)
  }

  fun testOperationName() = runBlockingCancellable {
    doTest()
  }

  fun testDirectiveArgument() = runBlockingCancellable {
    doTest(false)
  }

  private suspend fun doTest(withSchema: Boolean = true) {
    withContext(Dispatchers.EDT) {
      if (withSchema) {
        myFixture.copyFileToProject("${getTestName(true)}.graphql")
      }
      myFixture.configureByFile("${getTestName(true)}.js")
      myFixture.checkHighlighting()
    }
  }
}