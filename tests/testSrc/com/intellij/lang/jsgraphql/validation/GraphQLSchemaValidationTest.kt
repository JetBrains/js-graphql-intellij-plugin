package com.intellij.lang.jsgraphql.validation

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLSchemaValidationTest {
  private val graphQLFixture by graphQLTestFixture("/validation/schema", enableAllInspections = true)

  @Test
  fun testTypeRegistryRedefinitionErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testNotInputOutputTypesErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testSchemaValidatorErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testRedefinitionErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testBuiltInRedefinitionErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testImplementingErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testImplementingArgumentsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testEnumErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testDirectiveErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testSchemaDirectiveErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testObjectSchemaTypeExtensionsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testInterfaceSchemaTypeExtensionsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testUnionSchemaTypeExtensionsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testEnumSchemaTypeExtensionsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testScalarSchemaTypeExtensionsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testInputSchemaTypeExtensionsErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testRepeatableDirectivesErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testExtendSchemaErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testFieldDefinitionDirectiveErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testSchemaOperationTypeErrors() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testDeprecatedMembers() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testInterfaceImplementationWithFieldSubType() {
    graphQLFixture.doHighlightingTest()
  }

  @Test
  fun testDuplicatedTypeWithDescription() {
    graphQLFixture.doHighlightingTest()
  }
}
