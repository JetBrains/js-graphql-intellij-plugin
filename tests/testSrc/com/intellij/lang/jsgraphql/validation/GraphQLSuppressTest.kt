package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLSuppressTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/validation/suppress"
  }

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testSuppressOnDefinition() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testSuppressOnFile() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testSuppressOnStatementInjected() = runBlockingCancellable {
    doHighlightingTest("js")
  }

  fun testSuppressOnFileInjected() = runBlockingCancellable {
    doHighlightingTest("js")
  }
}
