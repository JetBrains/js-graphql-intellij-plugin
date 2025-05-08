package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager

class GraphQLJavaScriptFormatterTest : GraphQLTestCaseBase() {
  protected override fun getBasePath(): String = "/injection/formatter/js"

  fun testInjections() {
    doTest()
  }

  private fun doTest() {
    myFixture.configureByFile("${getTestName(true)}.js")
    myFixture.checkHighlighting()  // is needed to force language injections
    WriteCommandAction.runWriteCommandAction(project, Runnable {
      CodeStyleManager.getInstance(project).reformat(myFixture.getFile())
    })
    myFixture.checkResultByFile("${getTestName(true)}_after.js")
  }
}