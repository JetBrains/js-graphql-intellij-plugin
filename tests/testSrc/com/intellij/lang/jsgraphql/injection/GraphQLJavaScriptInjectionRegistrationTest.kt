/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.injection

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.lang.jsgraphql.types.language.AstPrinter
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors
import junit.framework.TestCase

class GraphQLJavaScriptInjectionRegistrationTest : GraphQLTestCaseBase() {
  override fun setUp() {
    super.setUp()

    enableAllInspections()
  }

  private suspend fun initPredefinedSchema() {
    edtWriteAction {
      myFixture.configureByFiles(
        "schema.graphql",
        ".graphqlconfig",
        "lines-1/.graphqlconfig",
        "lines-2/.graphqlconfig"
      )
    }

    myFixture.reloadGraphQLConfiguration()
  }

  override fun getBasePath(): String {
    return "/injection/registration/js"
  }

  fun testErrorAnnotatorOnFragments() = runBlockingCancellable {
    initPredefinedSchema()
    edtWriteAction { myFixture.configureByFiles("injection-comment.js") }
    myFixture.checkHighlighting()
    val highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR)
    TestCase.assertEquals("Expected just one error", 1, highlighting.size)
    TestCase.assertEquals("Unknown fragment name should be the error",
                          "OnlyTheUnknownFragmentShouldBeHighlightedAsError",
                          highlighting[0]!!.text)
  }

  fun testErrorAnnotatorSourceLines1() = runBlockingCancellable {
    initPredefinedSchema()
    edtWriteAction { myFixture.configureByFiles("lines-1/injection-source-lines-1.js") }
    myFixture.checkHighlighting()
    val highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR)
    TestCase.assertEquals("Expected just one error", 1, highlighting.size)
    TestCase.assertEquals("Should mark ServerType with an error", "ServerType", highlighting[0]!!.getText())
    TestCase.assertEquals("Should mark ServerType in the right injected position", 193, highlighting[0]!!.getStartOffset())
  }

  fun testErrorAnnotatorSourceLines2() = runBlockingCancellable {
    initPredefinedSchema()
    edtWriteAction { myFixture.configureByFiles("lines-2/injection-source-lines-2.js") }
    myFixture.checkHighlighting()
    val highlighting = myFixture.doHighlighting(HighlightSeverity.ERROR)
    TestCase.assertEquals("Expected just one error", 1, highlighting.size)
    TestCase.assertEquals("Should mark OutputType with an error", "OutputType", highlighting[0]!!.getText())
    TestCase.assertEquals("Should mark OutputType in the right injected position", 201, highlighting[0]!!.getStartOffset())
  }

  fun testInjectedTemplatesDontFail() = runBlockingCancellable {
    val injectedFile = doTestInjectedFile("injectedTemplates/injectedTemplates.js")
    myFixture.configureByText(GraphQLFileType.INSTANCE, readAction { AstPrinter.printAst((injectedFile as GraphQLFile).document) })
    myFixture.checkResultByFile("injectedTemplates/injectedTemplates_expected.graphql")
  }

  fun testInjectedWithEOLComment() = runBlockingCancellable<Unit> {
    doTestInjectedFile("eolComment.js")
  }

  fun testInjectedWithEOLComment1() = runBlockingCancellable<Unit> {
    doTestInjectedFile("eolComment1.js")
  }

  fun testInjectedWithEOLCommentInvalid() = runBlockingCancellable {
    doTestNoInjections("eolCommentInvalid.js")
  }

  fun testInjectedWithEOLCommentInvalid1() = runBlockingCancellable {
    doTestNoInjections("eolCommentInvalid1.js")
  }

  fun testInjectedWithCStyleComment() = runBlockingCancellable<Unit> {
    doTestInjectedFile("cStyleComment.js")
  }

  fun testInjectedWithCStyleCommentTagged() = runBlockingCancellable<Unit> {
    doTestInjectedFile("cStyleCommentTagged.js")
  }

  fun testInjectedWithCStyleCommentMultipleVars() = runBlockingCancellable<Unit> {
    doTestInjectedFile("cStyleCommentMultipleVars.js")
  }

  fun testInjectedInCallArgument() = runBlockingCancellable<Unit> {
    doTestInjectedFile("callArgument.js")
  }

  fun testInjectedInGqlCallArgument() = runBlockingCancellable<Unit> {
    doTestInjectedFile("gqlCallArgument.js")
  }

  fun testInjectedWithCStyleCommentAsType() = runBlockingCancellable<Unit> {
    doTestInjectedFile("cStyleCommentAsType.ts")
  }

  fun testInjectedWithCStyleCommentAsTypeTagged() = runBlockingCancellable<Unit> {
    doTestInjectedFile("cStyleCommentAsTypeTagged.ts")
  }

  private suspend fun doTestInjectedFile(sourcePath: String): PsiFile {
    edtWriteAction { myFixture.configureByFile(sourcePath) }
    val psiFiles = readAction {
      mutableListOf<PsiFile>().also {
        GraphQLPsiSearchHelper.getInstance(project).processInjectedGraphQLFiles(
          project, GlobalSearchScope.allScope(project), CommonProcessors.CollectProcessor(it)
        )
      }
    }
    assertSize(1, psiFiles)

    val injectedFile = psiFiles.first()
    assertInstanceOf(injectedFile, GraphQLFile::class.java)

    return injectedFile
  }

  private suspend fun doTestNoInjections(sourcePath: String) {
    edtWriteAction { myFixture.configureByFile(sourcePath) }
    val psiFiles = smartReadAction(project) {
      mutableListOf<PsiFile>().also {
        GraphQLPsiSearchHelper.getInstance(project).processInjectedGraphQLFiles(
          project, GlobalSearchScope.allScope(project), CommonProcessors.CollectProcessor(it))
      }
    }
    assertEmpty(psiFiles)
  }
}
