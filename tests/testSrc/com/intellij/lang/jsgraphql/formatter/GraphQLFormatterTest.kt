/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.formatter

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLFormatterTest {
  private val graphQLFixture by graphQLTestFixture("/formatter")

  @Test
  fun testSchema() {
    doTest()
  }

  private fun doTest() {
    graphQLFixture.fixture.configureByFiles("${graphQLFixture.getTestName(true)}.graphql")
    WriteCommandAction.runWriteCommandAction(graphQLFixture.project, Runnable {
      CodeStyleManager.getInstance(graphQLFixture.project).reformat(graphQLFixture.fixture.getFile())
    })
    graphQLFixture.fixture.checkResultByFile("${graphQLFixture.getTestName(true)}_after.graphql")
  }
}
