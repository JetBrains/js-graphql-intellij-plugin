package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLOperationsValidationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/validation/operations"
  }

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testRepeatableDirectives() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testWrongTypes() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testDuplicates() = runBlockingCancellable {
    doHighlightingTest()
  }
}
