package com.intellij.lang.jsgraphql.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.editor.LogicalPosition
import junit.framework.TestCase

class GraphQLIntrospectionLineMarkersTest : GraphQLTestCaseBase() {
  override fun getBasePath() = "/introspection/lineMarkers"

  fun testYaml() {
    doTest("graphql.config.yml", listOf(2, 5, 8, 9))
  }

  fun testYamlProjects() {
    // note that endpoints in the root are NOT introspected,
    // because when we have multiple projects, values on the root levels are treated as defaults only
    doTest("graphql.config.yml", listOf(15, 23, 31, 42, 45, 48, 49))
  }

  fun testYamlSchemaScalar() {
    doTest("graphql.config.yml", listOf(0))
  }

  fun testYamlUnresolvedEnvVariable() {
    doTest("graphql.config.yml", emptyList())
  }

  fun testYamlResolvedEnvVariable() {
    doTest("graphql.config.yml", listOf(0))
  }

  fun testJson() {
    doTest("graphql.config.json", listOf(4, 10, 14, 15))
  }

  fun testJsonProjects() {
    doTest("graphql.config.json", listOf(21, 34, 44, 61, 67, 71, 72))
  }

  fun testJsonSchemaScalar() {
    doTest("graphql.config.json", listOf(1))
  }

  private fun doTest(name: String, expectedGutterOffsets: List<Int>) {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile(name)
    reloadConfiguration()
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
