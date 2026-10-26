package com.intellij.lang.jsgraphql

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLDeprecatedSymbolsInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLDuplicateArgumentInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLDuplicateDirectiveInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLEmptyTypeInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLIllegalDirectiveArgumentInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLIllegalNameInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInterfaceImplementationInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLInvalidDirectiveLocationInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLMemberRedefinitionInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLMissingTypeInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLSchemaValidationInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLTypeRedefinitionInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnexpectedTypeInspection
import com.intellij.lang.jsgraphql.ide.validation.inspections.GraphQLUnresolvedReferenceInspection
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLNamedTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLNamedTypeExtension
import com.intellij.lang.jsgraphql.psi.impl.GraphQLIdentifierImpl
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightFixture
import com.intellij.platform.testFramework.junit5.codeInsight.fixture.codeInsightProjectFixture
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import java.io.IOException
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull

/**
 * Creates a [GraphQLTestFixture] on a new project. It is the JUnit 5 replacement of the JUnit 3 base `GraphQLTestCaseBase`.
 * Declare the result as an instance property, so that each test gets its own fixture and its own project.
 * [basePath] replaces an override of `GraphQLTestCaseBase.getBasePath()`. The test data path is [getTestDataPath] of [basePath].
 * [initTestProject] calls [GraphQLTestFixture.initTestProject] in the setup.
 * [enableAllInspections] calls [GraphQLTestFixture.enableAllInspections] in the setup, after [initTestProject].
 */
@TestOnly
fun graphQLTestFixture(
  basePath: String = "",
  enableAllInspections: Boolean = false,
  initTestProject: Boolean = false,
): TestFixture<GraphQLTestFixture> =
  graphQLTestFixture("graphQLTestFixture", basePath, enableAllInspections, initTestProject, ::GraphQLTestFixture)

/**
 * Creates a fixture of the [GraphQLTestFixture] chain. Each sub-fixture factory calls it, so that the setup of each level runs once.
 */
@TestOnly
internal fun <T : GraphQLTestFixture> graphQLTestFixture(
  debugString: String,
  basePath: String,
  enableAllInspections: Boolean,
  initTestProject: Boolean,
  create: (CodeInsightTestFixture, String) -> T,
): TestFixture<T> = testFixture(debugString) { context ->
  val fixture = codeInsightFixture(codeInsightProjectFixture()).init()
  fixture.testDataPath = getTestDataPath(basePath)
  val result = create(fixture, context.extensionContext.requiredTestMethod.name)
  result.setUp()
  if (initTestProject) {
    result.initTestProject()
  }
  if (enableAllInspections) {
    result.enableAllInspections()
  }
  initialized(result) {}
}

/**
 * The helpers of `GraphQLTestCaseBase` for a JUnit 5 test.
 * Call a member from a background thread. A member takes the read actions and the EDT calls that it needs.
 */
@TestOnly
open class GraphQLTestFixture internal constructor(
  val fixture: CodeInsightTestFixture,
  val testMethodName: String,
) {
  val project: Project get() = fixture.project

  val testRootDisposable: Disposable get() = fixture.testRootDisposable

  /**
   * The temp directory of [fixture]. The files of the test are in it.
   */
  val tempDir: VirtualFile get() = fixture.tempDirFixture.getFile("")!!

  /**
   * Returns the path of [file] relative to [tempDir].
   * Returns `null` when [file] is `null` or is not in [tempDir].
   */
  fun getRelativePath(file: VirtualFile?): String? = file?.let { VfsUtilCore.getRelativePath(it, tempDir) }

  /**
   * Returns the name of the test method without the `test` prefix.
   */
  fun getTestName(lowercaseFirstLetter: Boolean): String = PlatformTestUtil.getTestName(testMethodName, lowercaseFirstLetter)

  /**
   * Enables the GraphQL libraries and loads the GraphQL configuration. It is the `setUp()` of `GraphQLTestCaseBase`.
   */
  internal open suspend fun setUp() {
    val libraryManager = GraphQLLibraryManager.getInstance(project)
    libraryManager.enableLibraries(true)
    reloadGraphQLConfiguration(project)
    Disposer.register(testRootDisposable) {
      libraryManager.enableLibraries(false)
    }
  }

  /**
   * Copies the test data directory of the test to the project, and loads the GraphQL configuration again.
   * Returns the copied directory.
   */
  suspend fun initTestProject(lowerCaseFirstLetter: Boolean = true): VirtualFile {
    val dir = edtWriteAction { fixture.copyDirectoryToProject(getTestName(lowerCaseFirstLetter), "") }
    fixture.reloadGraphQLConfiguration()
    return dir
  }

  /**
   * Configures the file of the test with the [ext] extension, and checks its highlighting.
   */
  fun doHighlightingTest(ext: String = "graphql") {
    fixture.configureByFile(getTestName(false) + "." + ext)
    fixture.checkHighlighting()
  }

  /**
   * Enables the general GraphQL inspections and the schema inspections.
   */
  fun enableAllInspections() {
    fixture.enableInspections(GENERAL_INSPECTIONS)
    fixture.enableInspections(SCHEMA_INSPECTIONS)
  }

  /**
   * Reads [file] as a string with `\n` line separators.
   */
  fun readFileAsString(file: VirtualFile): String {
    try {
      return StringUtil.convertLineSeparators(VfsUtilCore.loadText(file))
    }
    catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  /**
   * Checks that [element] is an [expectedClass] with the [expectedName] name.
   */
  fun assertNamedElement(
    element: PsiElement?,
    expectedClass: Class<out PsiElement?>,
    expectedName: String,
  ) {
    assertInstanceOf(expectedClass, element)

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
        assertInstanceOf(PsiNamedElement::class.java, element)
        namedElement = element as PsiNamedElement
      }
    }
    assertNotNull(namedElement)
    assertEquals(expectedName, namedElement!!.getName())
  }

  /**
   * Checks that the definition that contains [element] is an [expectedClass] with the [expectedName] name.
   */
  suspend fun assertContainingDefinition(
    element: PsiElement?,
    expectedClass: Class<out PsiElement?>,
    expectedName: String,
  ) {
    assertNamedElement(readAction { GraphQLResolveUtil.findContainingDefinition(element) }, expectedClass, expectedName)
  }
}

private val GENERAL_INSPECTIONS = listOf<Class<out LocalInspectionTool>>(
  GraphQLUnresolvedReferenceInspection::class.java,
  GraphQLDeprecatedSymbolsInspection::class.java
)

// fake inspections for graphql-java validation
private val SCHEMA_INSPECTIONS = listOf<Class<out LocalInspectionTool>>(
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
