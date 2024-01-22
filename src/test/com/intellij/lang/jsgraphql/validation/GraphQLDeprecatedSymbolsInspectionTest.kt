package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase

class GraphQLDeprecatedSymbolsInspectionTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/validation/deprecated"

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testDeprecatedSymbols() {
    doHighlightingTest()
  }
}