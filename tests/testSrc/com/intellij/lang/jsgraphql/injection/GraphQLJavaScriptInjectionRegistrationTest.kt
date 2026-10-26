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
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.lang.jsgraphql.types.language.AstPrinter
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.CommonProcessors
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLJavaScriptInjectionRegistrationTest {
  private val graphQLFixture by graphQLTestFixture("/injection/registration/js", enableAllInspections = true)

  private suspend fun initPredefinedSchema() {
    graphQLFixture.fixture.configureByFiles(
      "schema.graphql",
      ".graphqlconfig",
      "lines-1/.graphqlconfig",
      "lines-2/.graphqlconfig"
    )

    graphQLFixture.fixture.reloadGraphQLConfiguration()
  }

  @Test
  fun testErrorAnnotatorOnFragments(): Unit = timeoutRunBlocking {
    initPredefinedSchema()
    graphQLFixture.fixture.configureByFiles("injection-comment.js")
    graphQLFixture.fixture.checkHighlighting()
    val highlighting = graphQLFixture.fixture.doHighlighting(HighlightSeverity.ERROR)
    assertEquals(1, highlighting.size, "Expected just one error")
    assertEquals("OnlyTheUnknownFragmentShouldBeHighlightedAsError",
                 highlighting[0]!!.text,
                 "Unknown fragment name should be the error")
  }

  @Test
  fun testErrorAnnotatorSourceLines1(): Unit = timeoutRunBlocking {
    initPredefinedSchema()
    graphQLFixture.fixture.configureByFiles("lines-1/injection-source-lines-1.js")
    graphQLFixture.fixture.checkHighlighting()
    val highlighting = graphQLFixture.fixture.doHighlighting(HighlightSeverity.ERROR)
    assertEquals(1, highlighting.size, "Expected just one error")
    assertEquals("ServerType", highlighting[0]!!.getText(), "Should mark ServerType with an error")
    assertEquals(193, highlighting[0]!!.getStartOffset(), "Should mark ServerType in the right injected position")
  }

  @Test
  fun testErrorAnnotatorSourceLines2(): Unit = timeoutRunBlocking {
    initPredefinedSchema()
    graphQLFixture.fixture.configureByFiles("lines-2/injection-source-lines-2.js")
    graphQLFixture.fixture.checkHighlighting()
    val highlighting = graphQLFixture.fixture.doHighlighting(HighlightSeverity.ERROR)
    assertEquals(1, highlighting.size, "Expected just one error")
    assertEquals("OutputType", highlighting[0]!!.getText(), "Should mark OutputType with an error")
    assertEquals(201, highlighting[0]!!.getStartOffset(), "Should mark OutputType in the right injected position")
  }

  @Test
  fun testInjectedTemplatesDontFail(): Unit = timeoutRunBlocking {
    val injectedFile = doTestInjectedFile("injectedTemplates/injectedTemplates.js")
    val text = readAction { AstPrinter.printAst((injectedFile as GraphQLFile).document) }
    graphQLFixture.fixture.configureByText(GraphQLFileType.INSTANCE, text)
    graphQLFixture.fixture.checkResultByFile("injectedTemplates/injectedTemplates_expected.graphql")
  }

  @Test
  fun testInjectedWithEOLComment(): Unit = timeoutRunBlocking {
    doTestInjectedFile("eolComment.js")
  }

  @Test
  fun testInjectedWithEOLComment1(): Unit = timeoutRunBlocking {
    doTestInjectedFile("eolComment1.js")
  }

  @Test
  fun testInjectedWithEOLCommentInvalid(): Unit = timeoutRunBlocking {
    doTestNoInjections("eolCommentInvalid.js")
  }

  @Test
  fun testInjectedWithEOLCommentInvalid1(): Unit = timeoutRunBlocking {
    doTestNoInjections("eolCommentInvalid1.js")
  }

  @Test
  fun testInjectedWithCStyleComment(): Unit = timeoutRunBlocking {
    doTestInjectedFile("cStyleComment.js")
  }

  @Test
  fun testInjectedWithCStyleCommentTagged(): Unit = timeoutRunBlocking {
    doTestInjectedFile("cStyleCommentTagged.js")
  }

  @Test
  fun testInjectedWithCStyleCommentMultipleVars(): Unit = timeoutRunBlocking {
    doTestInjectedFile("cStyleCommentMultipleVars.js")
  }

  @Test
  fun testInjectedInCallArgument(): Unit = timeoutRunBlocking {
    doTestInjectedFile("callArgument.js")
  }

  @Test
  fun testInjectedInGqlCallArgument(): Unit = timeoutRunBlocking {
    doTestInjectedFile("gqlCallArgument.js")
  }

  @Test
  fun testInjectedWithCStyleCommentAsType(): Unit = timeoutRunBlocking {
    doTestInjectedFile("cStyleCommentAsType.ts")
  }

  @Test
  fun testInjectedWithCStyleCommentAsTypeTagged(): Unit = timeoutRunBlocking {
    doTestInjectedFile("cStyleCommentAsTypeTagged.ts")
  }

  private suspend fun doTestInjectedFile(sourcePath: String): PsiFile {
    graphQLFixture.fixture.configureByFile(sourcePath)
    val psiFiles = readAction {
      mutableListOf<PsiFile>().also {
        GraphQLPsiSearchHelper.getInstance(graphQLFixture.project).processInjectedGraphQLFiles(
          graphQLFixture.project, GlobalSearchScope.allScope(graphQLFixture.project), CommonProcessors.CollectProcessor(it)
        )
      }
    }
    assertThat(psiFiles).hasSize(1)

    val injectedFile = psiFiles.first()
    assertInstanceOf(GraphQLFile::class.java, injectedFile)

    return injectedFile
  }

  private suspend fun doTestNoInjections(sourcePath: String) {
    graphQLFixture.fixture.configureByFile(sourcePath)
    val psiFiles = smartReadAction(graphQLFixture.project) {
      mutableListOf<PsiFile>().also {
        GraphQLPsiSearchHelper.getInstance(graphQLFixture.project).processInjectedGraphQLFiles(
          graphQLFixture.project, GlobalSearchScope.allScope(graphQLFixture.project), CommonProcessors.CollectProcessor(it))
      }
    }
    assertThat(psiFiles).isEmpty()
  }
}
