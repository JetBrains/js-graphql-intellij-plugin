package com.intellij.lang.jsgraphql.introspection

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.IOException
import kotlin.time.Duration.Companion.minutes

@TestApplication
class GraphQLIntrospectionPrinterTest {
  private val graphQLFixture by graphQLTestFixture("/introspection/print")

  @Test
  fun testPrintIntrospectionJsonAsGraphQL(): Unit = timeoutRunBlocking {
    doTest("schema.json", "schema.graphql")
  }

  @Test
  fun testPrintIntrospectionJsonWithEmptyErrorsAsGraphQL(): Unit = timeoutRunBlocking {
    doTest("schemaWithEmptyErrors.json", "schema.graphql")
  }

  @Test
  fun testPrintIntrospectionJsonWithErrorsAsGraphQL(): Unit = timeoutRunBlocking {
    try {
      doTest("schemaWithErrors.json", "schema.graphql")
    }
    catch (exception: IllegalArgumentException) {
      assertEquals("Introspection query returned errors: [{\"message\":\"Error\"}]", exception.message)
      return@timeoutRunBlocking
    }

    throw RuntimeException("Expected errors exception, found none.")
  }

  @Test
  fun testPrintIntrospectionWithNullFields(): Unit = timeoutRunBlocking {
    doTest("schemaWithNullFields.json", "schemaWithNullFields.graphql")
  }

  @Test
  fun testPrintIntrospectionRepeatableDirectives(): Unit = timeoutRunBlocking {
    doTest("schemaWithRepeatableDirectives.json", "schemaWithRepeatableDirectives.graphql")
  }

  @Test
  fun testGithubSchema() {
    // test only for being successful, file comparison doesn't give a meaningful result for files of this size
    assertDoesNotThrow {
      GraphQLIntrospectionService
        .printIntrospectionAsGraphQL(graphQLFixture.project, checkNotNull(readSchemaJson("githubSchema.json")))
    }
  }

  @Test
  fun testPrintIntrospectionWithUndefinedDirectives(): Unit = timeoutRunBlocking {
    doTest("schemaWithUndefinedDirectives.json", "schemaWithUndefinedDirectives.graphql")
  }

  @Test
  fun testPrintIntrospectionWithoutRootTypes(): Unit = timeoutRunBlocking {
    doTest("schemaWithoutRootTypes.json", "schemaWithoutRootTypes.graphql")
  }

  @Test
  fun testPrintIntrospectionWithCustomRootTypes(): Unit = timeoutRunBlocking {
    doTest("schemaWithCustomRootTypes.json", "schemaWithCustomRootTypes.graphql")
  }

  @Test
  fun testPrintIntrospectionWithJavaFormatterSpecifiersInDescriptions(): Unit = timeoutRunBlocking(1.minutes) {
    doTest("schemaWithJavaFormatterSpecifiersInDescriptions.json", "schemaWithJavaFormatterSpecifiersInDescriptions.graphql")
  }

  private suspend fun doTest(source: String, expected: String) {
    val introspection =
      GraphQLIntrospectionService.printIntrospectionAsGraphQL(graphQLFixture.project, checkNotNull(readSchemaJson(source)))
    graphQLFixture.fixture.configureByText("result.graphql", introspection)
    edtWriteAction {
      (graphQLFixture.fixture.getDocument(graphQLFixture.fixture.file) as DocumentImpl).stripTrailingSpaces(graphQLFixture.project, false)
    }
    graphQLFixture.fixture.checkResultByFile(expected)
  }

  private fun readSchemaJson(path: String): String? {
    try {
      return VfsUtilCore.loadText(graphQLFixture.fixture.copyFileToProject(path))
    }
    catch (_: IOException) {
      thisLogger().error("Failed to read $path")
      return null
    }
  }
}
