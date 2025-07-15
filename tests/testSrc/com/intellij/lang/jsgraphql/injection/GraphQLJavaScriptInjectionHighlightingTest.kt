package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.runBlockingCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphQLJavaScriptInjectionHighlightingTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/injection/highlighting/js"
  }

  fun testArgument() = runBlockingCancellable {
    doTest()
  }

  private suspend fun doTest() {
    withContext(Dispatchers.EDT) {
      val highlightTestInfo = myFixture.testFile(getTestName(true) + ".js")
      highlightTestInfo.checkSymbolNames()
      highlightTestInfo.test()
    }
  }
}
