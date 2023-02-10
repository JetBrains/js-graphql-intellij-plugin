package com.intellij.lang.jsgraphql

import com.intellij.lang.jsgraphql.ide.config.getPhysicalVirtualFile
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection
import com.intellij.lang.jsgraphql.psi.GraphQLIdentifier
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.VfsTestUtil
import junit.framework.TestCase

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

    protected fun doResolveAsTextTest(expectedClass: Class<out PsiElement>, expectedName: String): PsiElement {
        val reference = myFixture.getReferenceAtCaretPosition("${getTestName(false)}.graphql")
        TestCase.assertNotNull(reference)
        val element = reference!!.resolve()
        assertInstanceOf(element, PsiNamedElement::class.java)
        TestCase.assertEquals(expectedName, (element as PsiNamedElement?)!!.name)
        val definition = GraphQLResolveUtil.findResolvedDefinition(element)
        assertInstanceOf(definition, expectedClass)
        myFixture.checkHighlighting()
        return definition!!
    }

    protected fun doResolveWithOffsetTest(
        expectedClass: Class<out PsiElement>,
        expectedName: String
    ): PsiElement {
        val fileName = "${getTestName(false)}.graphql"
        val path = FileUtil.join(testDataPath, fileName)
        val file = VfsTestUtil.findFileByCaseSensitivePath(path)
        TestCase.assertNotNull(file)
        val text = readFileAsString(file)
        val textWithoutCarets = text.replace(CARET_MARK, "")
        val refOffset = textWithoutCarets.indexOf(REF_MARK)
        TestCase.assertTrue(refOffset >= 0)
        val psiFile = prepareFile(fileName, text)
        reloadConfiguration()
        val target = findElementAndResolve(psiFile)
        TestCase.assertEquals(target.textOffset, refOffset)
        assertInstanceOf(target, PsiNamedElement::class.java)
        TestCase.assertEquals(expectedName, (target as PsiNamedElement).name)
        val definition = GraphQLResolveUtil.findResolvedDefinition(target)
        assertInstanceOf(definition, expectedClass)
        myFixture.checkHighlighting()
        return definition!!
    }

    private fun prepareFile(fileName: String, text: String): PsiFile {
        return myFixture.configureByText(fileName, text.replace(REF_MARK, ""))
    }

    private fun findElementAndResolve(psiFile: PsiFile): PsiElement {
        val element: PsiElement? =
            PsiTreeUtil.getParentOfType(psiFile.findElementAt(myFixture.caretOffset), GraphQLIdentifier::class.java)
        TestCase.assertNotNull(element)
        val reference = element!!.reference
        TestCase.assertNotNull("Reference is null", reference)
        val target = reference!!.resolve()
        TestCase.assertNotNull("Resolved reference is null", target)
        return target!!
    }

    protected fun doProjectResolveTest(
        fileName: String,
        expectedClass: Class<out PsiElement>,
        expectedName: String,
        expectedFileName: String,
    ): PsiElement {
        loadProject()
        val psiFile = myFixture.configureFromTempProjectFile(fileName)
        TestCase.assertNotNull("given file is not found", psiFile)
        val target = findElementAndResolve(psiFile!!)
        assertInstanceOf(target, PsiNamedElement::class.java)
        TestCase.assertEquals(expectedName, (target as PsiNamedElement).name)
        val definition = GraphQLResolveUtil.findResolvedDefinition(target)
        assertInstanceOf(definition, expectedClass)
        val virtualFile = getPhysicalVirtualFile(definition?.containingFile)!!
        val expectedFile = myFixture.findFileInTempDir(expectedFileName)
        TestCase.assertNotNull("expected file not found: $expectedFileName", expectedFile)
        TestCase.assertEquals("resolved to wrong file", expectedFile.path, virtualFile.path)
        myFixture.checkHighlighting()
        return definition!!
    }

    protected fun doProjectHighlighting(fileName: String) {
        loadProject()
        myFixture.configureFromTempProjectFile(fileName)
        myFixture.checkHighlighting()
    }

    protected fun loadProject() {
        myFixture.copyDirectoryToProject(getTestName(false), "")
        reloadConfiguration()
    }
}
