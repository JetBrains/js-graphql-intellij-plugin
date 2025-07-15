package com.intellij.lang.jsgraphql.highlighting

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLHighlightingTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/highlighting"
  }

  fun testOperationDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testFragmentDefinition() = runBlockingCancellable {
    doTest()
  }

  fun testFragmentSpread() = runBlockingCancellable {
    doTest()
  }

  fun testTypeNameDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testFields() = runBlockingCancellable {
    doTest()
  }

  fun testFieldDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testInputFieldDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testArgumentDefinitions() = runBlockingCancellable {
    doTest()
  }

  fun testArguments() = runBlockingCancellable {
    doTest()
  }

  fun testVariables() = runBlockingCancellable {
    doTest()
  }

  fun testValues() = runBlockingCancellable {
    doTest()
  }

  fun testDirectives() = runBlockingCancellable {
    doTest()
  }

  fun testKeywordsAsIdentifiers() = runBlockingCancellable {
    doTest()
  }

  fun testSchemaDefinition() = runBlockingCancellable {
    doTest()
  }

  private suspend fun doTest() {
    val highlightTestInfo = myFixture.testFile(getTestName(false) + ".graphql")
    highlightTestInfo.checkSymbolNames()
    highlightTestInfo.test()
  }
}
