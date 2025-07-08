package com.intellij.lang.jsgraphql.documentation

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Computable
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager

class GraphQLDocumentationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/documentation"

  fun testVariable() {
    doTest()
  }

  fun testFragment() {
    doTest()
  }

  fun testOperation() {
    doTest()
  }

  private fun doTest() {
    val file = myFixture.configureByFile("${getTestName(false)}.graphql")
    val originalElement = file.findElementAt(myFixture.caretOffset)
    val element = DocumentationManager.getInstance(myFixture.project)
      .findTargetElement(myFixture.editor, file, originalElement)
    val provider = DocumentationManager.getProviderFromElement(element, originalElement)
    val doc = provider.generateDoc(element, originalElement)?.let { reformatDocumentation(project, it) } ?: "<empty>"
    assertSameLinesWithFile("${testDataPath}/${getTestName(false)}.html", doc)
  }

  internal fun reformatDocumentation(project: Project, text: String): String =
    WriteCommandAction.runWriteCommandAction(project, Computable {
      PsiFileFactory.getInstance(project).createFileFromText("doc.html", HTMLLanguage.INSTANCE, text)
        .let { CodeStyleManager.getInstance(project).reformat(it) }
        .text
    })
}