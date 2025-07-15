package com.intellij.lang.jsgraphql.documentation

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager

class GraphQLDocumentationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/documentation"

  fun testVariable() = runBlockingCancellable {
    doTest()
  }

  fun testFragment() = runBlockingCancellable {
    doTest()
  }

  fun testOperation() = runBlockingCancellable {
    doTest()
  }

  fun testField() = runBlockingCancellable {
    doTest()
  }

  fun testInputField() = runBlockingCancellable {
    doTest()
  }

  fun testDirectiveArgument() = runBlockingCancellable {
    doTest()
  }

  fun testEnumValue() = runBlockingCancellable {
    doTest()
  }

  private suspend fun doTest() {
    val file = myFixture.configureByFile("${getTestName(false)}.graphql")
    val originalElement = readAction { file.findElementAt(myFixture.caretOffset) }
    val element = readAction {
      DocumentationManager.getInstance(myFixture.project)
        .findTargetElement(myFixture.editor, file, originalElement)
    }
    val provider = readAction { DocumentationManager.getProviderFromElement(element, originalElement) }
    val doc = readAction { provider.generateDoc(element, originalElement) }?.let { reformatDocumentation(project, it) } ?: "<empty>"
    assertSameLinesWithFile("${testDataPath}/${getTestName(false)}.html", doc)
  }

  internal suspend fun reformatDocumentation(project: Project, text: String): String =
    writeCommandAction(project, "") {
      PsiFileFactory.getInstance(project).createFileFromText("doc.html", HTMLLanguage.INSTANCE, text)
        .let { CodeStyleManager.getInstance(project).reformat(it) }
        .text
    }
}