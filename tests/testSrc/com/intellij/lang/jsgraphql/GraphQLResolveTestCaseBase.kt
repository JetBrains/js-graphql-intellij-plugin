package com.intellij.lang.jsgraphql

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.VfsTestUtil
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class GraphQLResolveTestCaseBase : GraphQLTestCaseBase() {

  companion object {
    const val REF_MARK = "<ref>"
    const val CARET_MARK = "<caret>"
  }

  override fun setUp() {
    super.setUp()

    // all resolve tests should also check highlighting to ensure that the rest references are also resolved
    myFixture.enableInspections(
      listOf(GraphQLUnresolvedReferenceInspection::class.java)
    )
  }

  protected suspend fun doResolveAsTextTest(expectedClass: Class<out PsiElement>, expectedName: String): PsiElement {
    return withContext(Dispatchers.EDT) {
      val reference = myFixture.getReferenceAtCaretPosition("${getTestName(false)}.graphql")
      assertNotNull(reference)
      val element = smartReadAction(project) { reference!!.resolve() }
      assertInstanceOf(element, PsiNamedElement::class.java)
      TestCase.assertEquals(expectedName, readAction { (element as PsiNamedElement?)!!.name })
      val definition = readAction { GraphQLResolveUtil.adjustResolvedDefinition(element) }
      assertInstanceOf(definition, expectedClass)
      myFixture.checkHighlighting()
      definition!!
    }
  }

  protected suspend fun doResolveWithOffsetTest(
    expectedClass: Class<out PsiElement>,
    expectedName: String,
  ): PsiElement {
    return withContext(Dispatchers.EDT) {
      val fileName = "${getTestName(false)}.graphql"
      val path = FileUtil.join(testDataPath, fileName)
      val file = VfsTestUtil.findFileByCaseSensitivePath(path)
      assertNotNull(file)
      val text = readFileAsString(file)
      val textWithoutCarets = text.replace(CARET_MARK, "")
      val refOffset = textWithoutCarets.indexOf(REF_MARK)
      assertTrue(refOffset >= 0)
      val psiFile = prepareFile(fileName, text)
      withContext(Dispatchers.IO) {
        myFixture.reloadGraphQLConfiguration()
      }
      val target = findElementAndResolve(psiFile)
      TestCase.assertEquals(target.textOffset, refOffset)
      assertInstanceOf(target, PsiNamedElement::class.java)
      TestCase.assertEquals(expectedName, (target as PsiNamedElement).name)
      val definition = readAction { GraphQLResolveUtil.adjustResolvedDefinition(target) }
      assertInstanceOf(definition, expectedClass)
      myFixture.checkHighlighting()
      definition!!
    }
  }

  private fun prepareFile(fileName: String, text: String): PsiFile {
    return myFixture.configureByText(fileName, text.replace(REF_MARK, ""))
  }

  private suspend fun findElementAndResolve(psiFile: PsiFile): PsiElement {
    val element: PsiElement? =
      readAction { PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.caretOffset), GraphQLIdentifier::class.java) }
    assertNotNull(element)
    val reference = readAction { element!!.reference }
    assertNotNull("Reference is null", reference)
    val target = smartReadAction(project) { reference!!.resolve() }
    assertNotNull("Resolved reference is null", target)
    return target!!
  }

  protected suspend fun doProjectResolveTest(
    fileName: String,
    expectedClass: Class<out PsiElement>,
    expectedName: String,
    expectedFileName: String,
  ): PsiElement {
    initTestProject(false)

    return withContext(Dispatchers.EDT) {
      val psiFile = myFixture.configureFromTempProjectFile(fileName)
      assertNotNull("given file is not found", psiFile)
      val target = findElementAndResolve(psiFile!!)
      assertInstanceOf(target, PsiNamedElement::class.java)
      TestCase.assertEquals(expectedName, readAction { (target as PsiNamedElement).name })
      val definition = readAction { GraphQLResolveUtil.adjustResolvedDefinition(target) }
      assertInstanceOf(definition, expectedClass)
      val virtualFile = readAction { getPhysicalVirtualFile(definition?.containingFile)!! }
      val expectedFile = myFixture.findFileInTempDir(expectedFileName)
      assertNotNull("expected file not found: $expectedFileName", expectedFile)
      TestCase.assertEquals("resolved to wrong file", expectedFile.path, virtualFile.path)
      myFixture.checkHighlighting()
      definition!!
    }
  }

  protected suspend fun doProjectHighlighting(fileName: String) {
    initTestProject(false)

    withContext(Dispatchers.EDT) {
      myFixture.configureFromTempProjectFile(fileName)
      myFixture.checkHighlighting()
    }
  }
}
