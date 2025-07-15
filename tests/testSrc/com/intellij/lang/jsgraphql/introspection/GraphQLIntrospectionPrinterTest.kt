package com.intellij.lang.jsgraphql.introspection

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.introspection.GraphQLIntrospectionService
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.vfs.VfsUtilCore
import java.io.IOException

class GraphQLIntrospectionPrinterTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/introspection/print"

  fun testPrintIntrospectionJsonAsGraphQL() = runBlockingCancellable {
    doTest("schema.json", "schema.graphql")
  }

  fun testPrintIntrospectionJsonWithEmptyErrorsAsGraphQL() = runBlockingCancellable {
    doTest("schemaWithEmptyErrors.json", "schema.graphql")
  }

  fun testPrintIntrospectionJsonWithErrorsAsGraphQL() = runBlockingCancellable {
    try {
      doTest("schemaWithErrors.json", "schema.graphql")
    }
    catch (exception: IllegalArgumentException) {
      assertEquals("Introspection query returned errors: [{\"message\":\"Error\"}]", exception.message)
      return@runBlockingCancellable
    }

    throw RuntimeException("Expected errors exception, found none.")
  }

  fun testPrintIntrospectionWithNullFields() = runBlockingCancellable {
    doTest("schemaWithNullFields.json", "schemaWithNullFields.graphql")
  }

  fun testPrintIntrospectionRepeatableDirectives() = runBlockingCancellable {
    doTest("schemaWithRepeatableDirectives.json", "schemaWithRepeatableDirectives.graphql")
  }

  fun testGithubSchema() = runBlockingCancellable {
    // test only for being successful, file comparison doesn't give a meaningful result for files of this size
    assertNoThrowable {
      GraphQLIntrospectionService
        .printIntrospectionAsGraphQL(project, checkNotNull(readSchemaJson("githubSchema.json")))
    }
  }

  fun testPrintIntrospectionWithUndefinedDirectives() = runBlockingCancellable {
    doTest("schemaWithUndefinedDirectives.json", "schemaWithUndefinedDirectives.graphql")
  }

  fun testPrintIntrospectionWithoutRootTypes() = runBlockingCancellable {
    doTest("schemaWithoutRootTypes.json", "schemaWithoutRootTypes.graphql")
  }

  fun testPrintIntrospectionWithCustomRootTypes() = runBlockingCancellable {
    doTest("schemaWithCustomRootTypes.json", "schemaWithCustomRootTypes.graphql")
  }

  fun testPrintIntrospectionWithJavaFormatterSpecifiersInDescriptions() = runBlockingCancellable {
    doTest("schemaWithJavaFormatterSpecifiersInDescriptions.json", "schemaWithJavaFormatterSpecifiersInDescriptions.graphql")
  }

  private suspend fun doTest(source: String, expected: String) {
    val introspection =
      GraphQLIntrospectionService.printIntrospectionAsGraphQL(project, checkNotNull(readSchemaJson(source)))
    myFixture.configureByText("result.graphql", introspection)
    edtWriteAction { (myFixture.getDocument(myFixture.file) as DocumentImpl).stripTrailingSpaces(project, false) }
    myFixture.checkResultByFile(expected)
  }

  private fun readSchemaJson(path: String): String? {
    try {
      return VfsUtilCore.loadText(myFixture.copyFileToProject(path))
    }
    catch (_: IOException) {
      thisLogger().error("Failed to read $path")
      return null
    }
  }
}
