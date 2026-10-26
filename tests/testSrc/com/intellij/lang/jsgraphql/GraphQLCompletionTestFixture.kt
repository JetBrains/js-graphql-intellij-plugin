package com.intellij.lang.jsgraphql

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import com.intellij.testFramework.junit5.fixture.TestFixture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail

private const val DEFAULT_SCHEMA_FILENAME = "Schema.graphql"

/**
 * Creates a [GraphQLCompletionTestFixture] on a new project.
 * It is the JUnit 5 replacement of the JUnit 3 base `GraphQLCompletionTestCaseBase`.
 * Declare the result as an instance property, so that each test gets its own fixture and its own project.
 * [basePath] replaces an override of `GraphQLTestCaseBase.getBasePath()`.
 */
@TestOnly
fun graphQLCompletionTestFixture(
  basePath: String,
): TestFixture<GraphQLCompletionTestFixture> =
  graphQLTestFixture("graphQLCompletionTestFixture", basePath, enableAllInspections = false, initTestProject = false,
                     ::GraphQLCompletionTestFixture)

/**
 * The completion helpers of `GraphQLCompletionTestCaseBase` for a JUnit 5 test.
 * Call a member from a background thread. A member takes the EDT calls that it needs.
 */
@TestOnly
class GraphQLCompletionTestFixture internal constructor(
  fixture: CodeInsightTestFixture,
  testMethodName: String,
) : GraphQLTestFixture(fixture, testMethodName) {
  /**
   * Configures the `<test name>.graphql` file, and calls the basic completion.
   */
  fun doTest(): Array<LookupElement>? {
    val sourceFile = getTestName(false)
    fixture.configureByFile("$sourceFile.graphql")
    return fixture.complete(CompletionType.BASIC, 1)
  }

  /**
   * Copies [schemaFileName] to the project, then runs [doTest].
   */
  fun doTestWithSchema(schemaFileName: String = DEFAULT_SCHEMA_FILENAME): Array<LookupElement>? {
    fixture.copyFileToProject(schemaFileName)
    return doTest()
  }

  /**
   * Copies the test data directory of the test to the project, configures the `<test name><ext>` file, and calls the basic completion.
   */
  suspend fun doTestWithProject(ext: String = ".graphql"): Array<LookupElement>? {
    val dirName = getTestName(false)
    edtWriteAction { fixture.copyDirectoryToProject(dirName, "") }
    fixture.reloadGraphQLConfiguration()
    fixture.configureFromTempProjectFile(dirName + ext)
    return fixture.complete(CompletionType.BASIC, 1)
  }

  /**
   * Checks the result with the `<test name>_after.graphql` file.
   */
  fun checkResult() {
    val sourceFile = getTestName(false)
    fixture.checkResultByFile(sourceFile + "_after.graphql")
  }

  /**
   * Selects the [lookupString] item, types Enter, and checks the result with the `<test name>_after.graphql` file.
   */
  suspend fun checkResult(items: Array<LookupElement>?, lookupString: String) {
    val sourceFile = getTestName(false)
    val lookupElement = findLookupElement(items, lookupString)
    withContext(Dispatchers.EDT) {
      fixture.lookup.setCurrentItem(lookupElement)
    }
    fixture.type('\n')
    fixture.checkResultByFile(sourceFile + "_after.graphql")
  }

  /**
   * Checks that the lookup strings of [items] are [variants], in this order.
   */
  fun checkEqualsOrdered(items: Array<LookupElement>?, vararg variants: String) {
    checkNotNull(items)
    assertEquals(variants.toList(), items.map { obj -> obj.getLookupString() })
  }

  /**
   * Checks the type text of the [lookupString] item.
   */
  fun checkTypeText(items: Array<LookupElement>?, lookupString: String, expectedTypeText: String) {
    val lookupElement = findLookupElement(items, lookupString)
    checkTypeText(lookupElement, expectedTypeText)
  }

  /**
   * Checks the type text of [lookupElement].
   */
  fun checkTypeText(lookupElement: LookupElement, expectedTypeText: String) {
    assertEquals(expectedTypeText, TestLookupElementPresentation.renderReal(lookupElement).typeText)
  }

  /**
   * Checks the tail text of [lookupElement].
   */
  fun checkTailText(lookupElement: LookupElement, expectedTailText: String) {
    assertEquals(expectedTailText, TestLookupElementPresentation.renderReal(lookupElement).tailText)
  }

  /**
   * Checks that the [lookupString] item is struck out when [isDeprecated] is `true`.
   */
  fun checkDeprecated(items: Array<LookupElement>?, lookupString: String, isDeprecated: Boolean) {
    val lookupElement = findLookupElement(items, lookupString)
    checkDeprecated(lookupElement, isDeprecated)
  }

  /**
   * Checks that [lookupElement] is struck out when [isDeprecated] is `true`.
   */
  fun checkDeprecated(lookupElement: LookupElement, isDeprecated: Boolean) {
    assertEquals(isDeprecated, TestLookupElementPresentation.renderReal(lookupElement).isStrikeout)
  }

  /**
   * Checks that [items] contain all [variants].
   */
  fun checkContainsAll(items: Array<LookupElement>?, vararg variants: String) {
    checkNotNull(items)
    val variantsToCheck = mutableListOf(*variants)
    variantsToCheck.removeAll(items.map { obj -> obj.getLookupString() }.toSet())
    for (variant in variantsToCheck) {
      fail<Unit>("Missing completion variant: $variant")
    }
  }

  /**
   * Checks that [items] contain none of [variants]. A `null` [items] passes.
   */
  fun checkDoesNotContain(items: Array<LookupElement>?, vararg variants: String) {
    if (items == null) {
      return
    }

    val variantsToCheck = mutableListOf(*variants)
    variantsToCheck.retainAll(items.map { obj -> obj.getLookupString() }.toSet())
    for (variant in variantsToCheck) {
      fail<Unit>("Completion variant '$variant' must not exist")
    }
  }

  /**
   * Checks that [items] is not `null` and is empty.
   */
  fun checkEmpty(items: Array<LookupElement>?) {
    checkNotNull(items)
    assertTrue(items.isEmpty()) { "Expected no lookup elements, but got: ${items.map { it.lookupString }}" }
  }

  /**
   * Returns the item of [items] with the [lookupString] lookup string.
   */
  fun findLookupElement(items: Array<LookupElement>?, lookupString: String): LookupElement {
    checkNotNull(items)
    val lookupElement = items.find { element -> element.getLookupString() == lookupString }
    checkNotNull(lookupElement) { "Missing lookup element: $lookupString" }
    return lookupElement
  }
}
