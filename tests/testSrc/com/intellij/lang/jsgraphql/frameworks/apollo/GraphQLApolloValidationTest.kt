package com.intellij.lang.jsgraphql.frameworks.apollo

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLApolloValidationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/frameworks/apollo/validation"
  }

  fun testLocalFields() = runBlockingCancellable {
    doHighlightingTest()
  }
}
