/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.formatter

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager

class GraphQLFormatterTest : GraphQLTestCaseBase() {
  protected override fun getBasePath(): String = "/formatter"

  fun testSchema() {
    doTest()
  }

  private fun doTest() {
    myFixture.configureByFiles("${getTestName(true)}.graphql")
    WriteCommandAction.runWriteCommandAction(project, Runnable {
      CodeStyleManager.getInstance(project).reformat(myFixture.getFile())
    })
    myFixture.checkResultByFile("${getTestName(true)}_after.graphql")
  }
}
