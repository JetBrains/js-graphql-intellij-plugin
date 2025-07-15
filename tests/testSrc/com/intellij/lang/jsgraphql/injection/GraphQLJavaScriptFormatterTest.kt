package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.application.EDT
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.psi.codeStyle.CodeStyleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphQLJavaScriptFormatterTest : GraphQLTestCaseBase() {
  protected override fun getBasePath(): String = "/injection/formatter/js"

  fun testInjections() = runBlockingCancellable {
    doTest()
  }

  private suspend fun doTest() {
    myFixture.configureByFile("${getTestName(true)}.js")
    withContext(Dispatchers.EDT) { myFixture.checkHighlighting() }  // is needed to force language injections
    writeCommandAction(project, "") {
      CodeStyleManager.getInstance(project).reformat(myFixture.getFile())
    }
    myFixture.checkResultByFile("${getTestName(true)}_after.js")
  }
}