package com.intellij.lang.jsgraphql.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLIntrospectionLineMarkersTest {
  private val graphQLFixture by graphQLTestFixture("/introspection/lineMarkers")

  @Test
  fun testYaml(): Unit = timeoutRunBlocking {
    doTest("graphql.config.yml", listOf(2, 5, 8, 9))
  }

  @Test
  fun testYamlProjects(): Unit = timeoutRunBlocking {
    // note that endpoints in the root are NOT introspected,
    // because when we have multiple projects, values on the root levels are treated as defaults only
    doTest("graphql.config.yml", listOf(15, 23, 31, 42, 45, 48, 49))
  }

  @Test
  fun testYamlSchemaScalar(): Unit = timeoutRunBlocking {
    doTest("graphql.config.yml", listOf(0))
  }

  @Test
  fun testYamlUnresolvedEnvVariable(): Unit = timeoutRunBlocking {
    doTest("graphql.config.yml", emptyList())
  }

  @Test
  fun testYamlResolvedEnvVariable(): Unit = timeoutRunBlocking {
    doTest("graphql.config.yml", listOf(0))
  }

  @Test
  fun testJson(): Unit = timeoutRunBlocking {
    doTest("graphql.config.json", listOf(4, 10, 14, 15))
  }

  @Test
  fun testJsonProjects(): Unit = timeoutRunBlocking {
    doTest("graphql.config.json", listOf(21, 34, 44, 61, 67, 71, 72))
  }

  @Test
  fun testJsonSchemaScalar(): Unit = timeoutRunBlocking {
    doTest("graphql.config.json", listOf(1))
  }

  private suspend fun doTest(name: String, expectedGutterOffsets: List<Int>) {
    edtWriteAction {
      graphQLFixture.fixture.copyDirectoryToProject(graphQLFixture.getTestName(true), "")
      graphQLFixture.fixture.configureFromTempProjectFile(name)
    }
    graphQLFixture.fixture.reloadGraphQLConfiguration()
    withContext(Dispatchers.EDT) {
      val allGutters = graphQLFixture.fixture.findAllGutters()
      assertEquals(expectedGutterOffsets.size, allGutters.size, "total gutters count")
      expectedGutterOffsets.forEach {
        graphQLFixture.fixture.editor.caretModel.moveToLogicalPosition(LogicalPosition(it, 0))
        val gutters = graphQLFixture.fixture.findGuttersAtCaret()
        assertEquals(1, gutters.size, "invalid gutters count at line $it")
        assertEquals(
          GraphQLBundle.message("graphql.introspection.run.query"),
          gutters[0].tooltipText,
          "invalid gutter tooltip at line $it",
        )
      }
    }
  }
}
