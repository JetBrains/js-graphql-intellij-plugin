package com.intellij.lang.jsgraphql.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.progress.runBlockingCancellable
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GraphQLIntrospectionLineMarkersTest : GraphQLTestCaseBase() {
  override fun getBasePath() = "/introspection/lineMarkers"

  fun testYaml() = runBlockingCancellable {
    doTest("graphql.config.yml", listOf(2, 5, 8, 9))
  }

  fun testYamlProjects() = runBlockingCancellable {
    // note that endpoints in the root are NOT introspected,
    // because when we have multiple projects, values on the root levels are treated as defaults only
    doTest("graphql.config.yml", listOf(15, 23, 31, 42, 45, 48, 49))
  }

  fun testYamlSchemaScalar() = runBlockingCancellable {
    doTest("graphql.config.yml", listOf(0))
  }

  fun testYamlUnresolvedEnvVariable() = runBlockingCancellable {
    doTest("graphql.config.yml", emptyList())
  }

  fun testYamlResolvedEnvVariable() = runBlockingCancellable {
    doTest("graphql.config.yml", listOf(0))
  }

  fun testJson() = runBlockingCancellable {
    doTest("graphql.config.json", listOf(4, 10, 14, 15))
  }

  fun testJsonProjects() = runBlockingCancellable {
    doTest("graphql.config.json", listOf(21, 34, 44, 61, 67, 71, 72))
  }

  fun testJsonSchemaScalar() = runBlockingCancellable {
    doTest("graphql.config.json", listOf(1))
  }

  private suspend fun doTest(name: String, expectedGutterOffsets: List<Int>) {
    edtWriteAction {
      myFixture.copyDirectoryToProject(getTestName(true), "")
      myFixture.configureFromTempProjectFile(name)
    }
    myFixture.reloadGraphQLConfiguration()
    withContext(Dispatchers.EDT) {
      val allGutters = myFixture.findAllGutters()
      TestCase.assertEquals("total gutters count", expectedGutterOffsets.size, allGutters.size)
      expectedGutterOffsets.forEach {
        myFixture.editor.caretModel.moveToLogicalPosition(LogicalPosition(it, 0))
        val gutters = myFixture.findGuttersAtCaret()
        TestCase.assertEquals("invalid gutters count at line $it", 1, gutters.size)
        TestCase.assertEquals(
          "invalid gutter tooltip at line $it",
          GraphQLBundle.message("graphql.introspection.run.query"),
          gutters[0].tooltipText,
        )
      }
    }
  }
}
