package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLSchemaValidationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String {
    return "/validation/schema"
  }

  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  fun testTypeRegistryRedefinitionErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testNotInputOutputTypesErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testSchemaValidatorErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testRedefinitionErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testBuiltInRedefinitionErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testImplementingErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testImplementingArgumentsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testEnumErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testDirectiveErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testSchemaDirectiveErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testObjectSchemaTypeExtensionsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testInterfaceSchemaTypeExtensionsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testUnionSchemaTypeExtensionsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testEnumSchemaTypeExtensionsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testScalarSchemaTypeExtensionsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testInputSchemaTypeExtensionsErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testRepeatableDirectivesErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testExtendSchemaErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testFieldDefinitionDirectiveErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testSchemaOperationTypeErrors() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testDeprecatedMembers() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testInterfaceImplementationWithFieldSubType() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testDuplicatedTypeWithDescription() = runBlockingCancellable {
    doHighlightingTest()
  }
}
