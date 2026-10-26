package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.openapi.command.writeCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLJavaScriptFormatterTest {
  private val graphQLFixture by graphQLTestFixture("/injection/formatter/js")

  @Test
  fun testInjections(): Unit = timeoutRunBlocking {
    doTest()
  }

  private suspend fun doTest() {
    graphQLFixture.fixture.configureByFile("${graphQLFixture.getTestName(true)}.js")
    graphQLFixture.fixture.checkHighlighting()  // is needed to force language injections
    writeCommandAction(graphQLFixture.project, "") {
      CodeStyleManager.getInstance(graphQLFixture.project).reformat(graphQLFixture.fixture.getFile())
    }
    graphQLFixture.fixture.checkResultByFile("${graphQLFixture.getTestName(true)}_after.js")
  }
}