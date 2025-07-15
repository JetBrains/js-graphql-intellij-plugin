package com.intellij.lang.jsgraphql

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.validation.inspections.*
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLNamedTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLNamedTypeExtension
import com.intellij.lang.jsgraphql.psi.impl.GraphQLIdentifierImpl
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager.Companion.getInstance
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase
import java.io.IOException

abstract class GraphQLTestCaseBase : BasePlatformTestCase() {
  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()

    runBlockingCancellable {
      val libraryManager = getInstance(project)
      libraryManager.enableLibraries(true)
      reloadGraphQLConfiguration(project)
      Disposer.register(testRootDisposable) {
        libraryManager.enableLibraries(false)
      }
    }
  }

  protected override fun runFromCoroutine(): Boolean {
    return true
  }

  protected override fun runInDispatchThread(): Boolean {
    return false
  }

  protected override fun getTestDataPath(): String {
    return getTestDataPath(basePath)
  }

  protected suspend fun initTestProject(lowerCaseFirstLetter: Boolean = true): VirtualFile {
    val dir = edtWriteAction { myFixture.copyDirectoryToProject(getTestName(lowerCaseFirstLetter), "") }
    myFixture.reloadGraphQLConfiguration()
    return dir
  }

  protected fun doHighlightingTest(ext: String = "graphql") {
    myFixture.configureByFile(getTestName(false) + "." + ext)
    myFixture.checkHighlighting()
  }

  protected fun enableAllInspections() {
    myFixture.enableInspections(ourGeneralInspections)
    myFixture.enableInspections(ourSchemaInspections)
  }

  protected fun readFileAsString(file: VirtualFile): String {
    try {
      return StringUtil.convertLineSeparators(VfsUtilCore.loadText(file))
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  protected fun assertNamedElement(
    element: PsiElement?,
    expectedClass: Class<out PsiElement?>,
    expectedName: String,
  ) {
    assertInstanceOf(element, expectedClass)

    val namedElement: PsiNamedElement?
    when (element) {
      is GraphQLNamedTypeDefinition -> namedElement = element.getTypeNameDefinition()
      is GraphQLNamedTypeExtension -> namedElement = element.getTypeName()
      is GraphQLDirectiveDefinition -> {
        // for some reason elements which are supposed to implement PsiNamedElement don't implement it
        // GraphQLIdentifier interface also doesn't, so we need this explicit downcast to the GraphQLIdentifierImpl
        namedElement = (element.getNameIdentifier() as GraphQLIdentifierImpl?)
      }
      else -> {
        assertInstanceOf(element, PsiNamedElement::class.java)
        namedElement = element as PsiNamedElement
      }
    }
    assertNotNull(namedElement)
    TestCase.assertEquals(expectedName, namedElement!!.getName())
  }

  protected suspend fun assertContainingDefinition(
    element: PsiElement?,
    expectedClass: Class<out PsiElement?>,
    expectedName: String,
  ) {
    assertNamedElement(readAction { GraphQLResolveUtil.findContainingDefinition(element) }, expectedClass, expectedName)
  }

  companion object {
    protected val ourGeneralInspections = listOf<Class<out LocalInspectionTool>>(
      GraphQLUnresolvedReferenceInspection::class.java,
      GraphQLDeprecatedSymbolsInspection::class.java
    )

    // fake inspections for graphql-java validation
    protected val ourSchemaInspections = listOf<Class<out LocalInspectionTool>>(
      GraphQLSchemaValidationInspection::class.java,
      GraphQLTypeRedefinitionInspection::class.java,
      GraphQLUnexpectedTypeInspection::class.java,
      GraphQLMemberRedefinitionInspection::class.java,
      GraphQLIllegalNameInspection::class.java,
      GraphQLDuplicateArgumentInspection::class.java,
      GraphQLEmptyTypeInspection::class.java,
      GraphQLInterfaceImplementationInspection::class.java,
      GraphQLDuplicateDirectiveInspection::class.java,
      GraphQLMissingTypeInspection::class.java,
      GraphQLIllegalDirectiveArgumentInspection::class.java,
      GraphQLInvalidDirectiveLocationInspection::class.java
    )
  }
}
