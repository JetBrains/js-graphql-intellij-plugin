package com.intellij.lang.jsgraphql.frameworks.relay

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes
import com.intellij.lang.jsgraphql.withLibrary
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLRelayValidationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/frameworks/relay/validation"
  }

  fun testSuppressedInspections() = runBlockingCancellable {
    withLibrary(project, GraphQLBundledLibraryTypes.RELAY) { doHighlightingTest() }
  }
}
