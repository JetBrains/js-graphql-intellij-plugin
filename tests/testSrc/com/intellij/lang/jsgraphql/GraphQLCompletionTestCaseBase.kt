package com.intellij.lang.jsgraphql

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.testFramework.fixtures.TestLookupElementPresentation
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEFAULT_SCHEMA_FILENAME = "Schema.graphql"

abstract class GraphQLCompletionTestCaseBase : GraphQLTestCaseBase() {
  protected suspend fun doTest(): Array<LookupElement>? {
    return withContext(Dispatchers.EDT) {
      val sourceFile = getTestName(false)
      myFixture.configureByFile("$sourceFile.graphql")
      myFixture.complete(CompletionType.BASIC, 1)
    }
  }

  protected suspend fun doTestWithSchema(schemaFileName: String = DEFAULT_SCHEMA_FILENAME): Array<LookupElement>? {
    return withContext(Dispatchers.EDT) {
      myFixture.copyFileToProject(schemaFileName)
      doTest()
    }
  }

  protected suspend fun doTestWithProject(ext: String = ".graphql"): Array<LookupElement>? {
    val dirName = getTestName(false)
    edtWriteAction { myFixture.copyDirectoryToProject(dirName, "") }
    myFixture.reloadGraphQLConfiguration()
    myFixture.configureFromTempProjectFile(dirName + ext)
    return withContext(Dispatchers.EDT) {
      myFixture.complete(CompletionType.BASIC, 1)
    }
  }

  protected suspend fun checkResult() {
    withContext(Dispatchers.EDT) {
      val sourceFile = getTestName(false)
      myFixture.checkResultByFile(sourceFile + "_after.graphql")
    }
  }

  protected suspend fun checkResult(items: Array<LookupElement>?, lookupString: String) {
    withContext(Dispatchers.EDT) {
      val sourceFile = getTestName(false)
      val lookupElement = findLookupElement(items, lookupString)
      myFixture.lookup.setCurrentItem(lookupElement)
      myFixture.type('\n')
      myFixture.checkResultByFile(sourceFile + "_after.graphql")
    }
  }

  protected fun checkEqualsOrdered(items: Array<LookupElement>?, vararg variants: String) {
    checkNotNull(items)
    assertOrderedEquals(items.map { obj -> obj.getLookupString() }, *variants)
  }

  protected fun checkTypeText(items: Array<LookupElement>?, lookupString: String, expectedTypeText: String) {
    val lookupElement = findLookupElement(items, lookupString)
    checkTypeText(lookupElement, expectedTypeText)
  }

  protected fun checkTypeText(lookupElement: LookupElement, expectedTypeText: String) {
    assertEquals(expectedTypeText, TestLookupElementPresentation.renderReal(lookupElement).typeText)
  }

  protected fun checkTailText(items: Array<LookupElement>?, lookupString: String, expectedTailText: String) {
    val lookupElement = findLookupElement(items, lookupString)
    checkTailText(lookupElement, expectedTailText)
  }

  protected fun checkTailText(lookupElement: LookupElement, expectedTailText: String) {
    assertEquals(expectedTailText, TestLookupElementPresentation.renderReal(lookupElement).tailText)
  }

  protected fun checkDeprecated(items: Array<LookupElement>?, lookupString: String, isDeprecated: Boolean) {
    val lookupElement = findLookupElement(items, lookupString)
    checkDeprecated(lookupElement, isDeprecated)
  }

  protected fun checkDeprecated(lookupElement: LookupElement, isDeprecated: Boolean) {
    TestCase.assertEquals(isDeprecated, TestLookupElementPresentation.renderReal(lookupElement).isStrikeout)
  }

  protected fun checkContainsAll(items: Array<LookupElement>?, vararg variants: String) {
    checkNotNull(items)
    val variantsToCheck = mutableListOf(*variants)
    variantsToCheck.removeAll(items.map { obj -> obj.getLookupString() }.toSet())
    for (variant in variantsToCheck) {
      fail("Missing completion variant: $variant")
    }
  }

  protected fun checkDoesNotContain(items: Array<LookupElement>?, vararg variants: String) {
    if (items == null) {
      return
    }

    val variantsToCheck = mutableListOf(*variants)
    variantsToCheck.retainAll(items.map { obj -> obj.getLookupString() }.toSet())
    for (variant in variantsToCheck) {
      fail("Completion variant '$variant' must not exist")
    }
  }

  protected fun checkEmpty(items: Array<LookupElement>?) {
    checkNotNull(items)
    assertEmpty(items)
  }

  protected fun findLookupElement(items: Array<LookupElement>?, lookupString: String): LookupElement {
    checkNotNull(items)
    val lookupElement = items.find { element -> element.getLookupString() == lookupString }
    checkNotNull(lookupElement) { "Missing lookup element: $lookupString" }
    return lookupElement
  }
}
