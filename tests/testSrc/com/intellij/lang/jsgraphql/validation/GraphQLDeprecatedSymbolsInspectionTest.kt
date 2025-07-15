package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLDeprecatedSymbolsInspectionTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/validation/deprecated"

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testDeprecatedSymbols() = runBlockingCancellable {
    doHighlightingTest()
  }
}