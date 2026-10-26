package com.intellij.lang.jsgraphql

import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier
import com.intellij.lang.jsgraphql.psi.getPhysicalVirtualFile
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.VfsTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Creates a [GraphQLResolveTestFixture] on a new project.
 * It is the JUnit 5 replacement of the JUnit 3 base `GraphQLResolveTestCaseBase`.
 * Declare the result as an instance property, so that each test gets its own fixture and its own project.
 * [basePath] replaces an override of `GraphQLTestCaseBase.getBasePath()`.
 */
@TestOnly
fun graphQLResolveTestFixture(
  basePath: String,
): TestFixture<GraphQLResolveTestFixture> =
  graphQLTestFixture("graphQLResolveTestFixture", basePath, enableAllInspections = false, initTestProject = false,
                     ::GraphQLResolveTestFixture)

/**
 * The resolve helpers of `GraphQLResolveTestCaseBase` for a JUnit 5 test.
 * The setup enables [GraphQLUnresolvedReferenceInspection].
 * Call a member from a background thread. A member takes the read actions and the EDT calls that it needs.
 */
@TestOnly
class GraphQLResolveTestFixture internal constructor(
  fixture: CodeInsightTestFixture,
  testMethodName: String,
) : GraphQLTestFixture(fixture, testMethodName) {
  companion object {
    const val REF_MARK: String = "<ref>"
    const val CARET_MARK: String = "<caret>"
  }

  override suspend fun setUp() {
    super.setUp()

    // all resolve tests should also check highlighting to ensure that the rest references are also resolved
    fixture.enableInspections(
      listOf(GraphQLUnresolvedReferenceInspection::class.java)
    )
  }

  /**
   * Resolves the reference at the caret of the `<test name>.graphql` file.
   * Checks the name of the target, the class of its definition, and the highlighting. Returns the definition.
   */
  suspend fun doResolveAsTextTest(expectedClass: Class<out PsiElement>, expectedName: String): PsiElement {
    val reference = fixture.getReferenceAtCaretPosition("${getTestName(false)}.graphql")
    assertNotNull(reference)
    val element = smartReadAction(project) { reference!!.resolve() }
    assertInstanceOf(PsiNamedElement::class.java, element)
    assertEquals(expectedName, readAction { (element as PsiNamedElement?)!!.name })
    val definition = readAction { GraphQLResolveUtil.adjustResolvedDefinition(element) }
    assertInstanceOf(expectedClass, definition)
    fixture.checkHighlighting()
    return definition!!
  }

  /**
   * Resolves the reference at the caret of the `<test name>.graphql` file, and checks that the target is at the `<ref>` mark.
   * Checks the name of the target, the class of its definition, and the highlighting. Returns the definition.
   */
  suspend fun doResolveWithOffsetTest(
    expectedClass: Class<out PsiElement>,
    expectedName: String,
  ): PsiElement {
    val fileName = "${getTestName(false)}.graphql"
    val path = FileUtil.join(fixture.testDataPath, fileName)
    val file = VfsTestUtil.findFileByCaseSensitivePath(path)
    assertNotNull(file)
    val text = readFileAsString(file)
    val textWithoutCarets = text.replace(CARET_MARK, "")
    val refOffset = textWithoutCarets.indexOf(REF_MARK)
    assertTrue(refOffset >= 0)
    val psiFile = prepareFile(fileName, text)
    fixture.reloadGraphQLConfiguration()
    val target = findElementAndResolve(psiFile)
    assertEquals(readAction { target.textOffset }, refOffset)
    assertInstanceOf(PsiNamedElement::class.java, target)
    assertEquals(expectedName, readAction { (target as PsiNamedElement).name })
    val definition = readAction { GraphQLResolveUtil.adjustResolvedDefinition(target) }
    assertInstanceOf(expectedClass, definition)
    fixture.checkHighlighting()
    return definition!!
  }

  private fun prepareFile(fileName: String, text: String): PsiFile {
    return fixture.configureByText(fileName, text.replace(REF_MARK, ""))
  }

  private suspend fun findElementAndResolve(psiFile: PsiFile): PsiElement {
    val element: PsiElement? =
      readAction { PsiTreeUtil.getParentOfType(psiFile.findElementAt(fixture.caretOffset), GraphQLIdentifier::class.java) }
    assertNotNull(element)
    val reference = readAction { element!!.reference }
    assertNotNull(reference, "Reference is null")
    val target = smartReadAction(project) { reference!!.resolve() }
    assertNotNull(target, "Resolved reference is null")
    return target!!
  }

  /**
   * Copies the test data directory of the test to the project, and resolves the reference at the caret of [fileName].
   * Checks the name and the file of the target, the class of its definition, and the highlighting. Returns the definition.
   */
  suspend fun doProjectResolveTest(
    fileName: String,
    expectedClass: Class<out PsiElement>,
    expectedName: String,
    expectedFileName: String,
  ): PsiElement {
    initTestProject(false)

    val psiFile = fixture.configureFromTempProjectFile(fileName)
    assertNotNull(psiFile, "given file is not found")
    val target = findElementAndResolve(psiFile!!)
    assertInstanceOf(PsiNamedElement::class.java, target)
    assertEquals(expectedName, readAction { (target as PsiNamedElement).name })
    val definition = readAction { GraphQLResolveUtil.adjustResolvedDefinition(target) }
    assertInstanceOf(expectedClass, definition)
    val virtualFile = readAction { getPhysicalVirtualFile(definition?.containingFile)!! }
    val expectedFile = fixture.findFileInTempDir(expectedFileName)
    assertNotNull(expectedFile, "expected file not found: $expectedFileName")
    assertEquals(expectedFile.path, virtualFile.path, "resolved to wrong file")
    fixture.checkHighlighting()
    return definition!!
  }

  /**
   * Copies the test data directory of the test to the project, and checks the highlighting of [fileName].
   */
  suspend fun doProjectHighlighting(fileName: String) {
    initTestProject(false)

    fixture.configureFromTempProjectFile(fileName)
    fixture.checkHighlighting()
  }
}
