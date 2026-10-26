package com.intellij.lang.jsgraphql.documentation

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLDocumentationTest {
  private val graphQLFixture by graphQLTestFixture("/documentation")

  @Test
  fun testVariable(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testFragment(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testOperation(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testField(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testInputField(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDirectiveArgument(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testEnumValue(): Unit = timeoutRunBlocking {
    doTest()
  }

  private suspend fun doTest() {
    val file = graphQLFixture.fixture.configureByFile("${graphQLFixture.getTestName(false)}.graphql")
    val originalElement = readAction { file.findElementAt(graphQLFixture.fixture.caretOffset) }
    val element = readAction {
      DocumentationManager.getInstance(graphQLFixture.project)
        .findTargetElement(graphQLFixture.fixture.editor, file, originalElement)
    }
    val provider = readAction { DocumentationManager.getProviderFromElement(element, originalElement) }
    val doc = readAction { provider.generateDoc(element, originalElement) }
      ?.let { reformatDocumentation(graphQLFixture.project, it) } ?: "<empty>"
    PlatformTestUtil.assertSameLinesWithFile("${graphQLFixture.fixture.testDataPath}/${graphQLFixture.getTestName(false)}.html", doc)
  }

  internal suspend fun reformatDocumentation(project: Project, text: String): String =
    writeCommandAction(project, "") {
      PsiFileFactory.getInstance(project).createFileFromText("doc.html", HTMLLanguage.INSTANCE, text)
        .let { CodeStyleManager.getInstance(project).reformat(it) }
        .text
    }
}