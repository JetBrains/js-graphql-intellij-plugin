package com.intellij.lang.jsgraphql.navbar

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.platform.navbar.testFramework.contextNavBarPathStrings
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLNavBarTest {
  private val graphQLFixture by graphQLTestFixture()

  @Test
  fun testNamedQuery(): Unit = doTest(
    """
      query MyQuery {
        us<caret>er
      }
    """.trimIndent(),
    "MyQuery", "user"
  )

  @Test
  fun testAnonymousQuery(): Unit = doTest(
    """
      {
        us<caret>er
      }
    """.trimIndent(),
    "anonymous query", "user"
  )

  @Test
  fun testNestedField(): Unit = doTest(
    """
      query MyQuery {
        user {
          na<caret>me
        }
      }
    """.trimIndent(),
    "MyQuery", "user", "name"
  )

  @Test
  fun testFragmentDefinition(): Unit = doTest(
    """
      fragment MyFragment on User {
        na<caret>me
      }
    """.trimIndent(),
    "MyFragment", "name"
  )

  @Test
  fun testInlineFragment(): Unit = doTest(
    """
      query MyQuery {
        user {
          ... on User {
            na<caret>me
          }
        }
      }
    """.trimIndent(),
    "MyQuery", "user", "... on User", "name"
  )

  @Test
  fun testObjectTypeDefinition(): Unit = doTest(
    """
      type User {
        na<caret>me: String
      }
    """.trimIndent(),
    "User", "name"
  )

  @Test
  fun testFieldArgumentDefinition(): Unit = doTest(
    """
      type Query {
        user(i<caret>d: ID): User
      }
    """.trimIndent(),
    "Query", "user", "id"
  )

  @Test
  fun testEnumTypeDefinition(): Unit = doTest(
    """
      enum Color {
        RE<caret>D
        GREEN
      }
    """.trimIndent(),
    "Color", "RED"
  )

  @Test
  fun testFileRoot(): Unit = doTest(
    """
      query MyQuery {
        user
      }
      <caret>
    """.trimIndent()
  )

  private fun doTest(@Language("GraphQL") text: String, vararg expectedItems: String): Unit = timeoutRunBlocking {
    graphQLFixture.fixture.configureByText("test.graphql", text)
    val dataContext = (graphQLFixture.fixture.editor as EditorEx).dataContext
    val items = readAction { contextNavBarPathStrings(dataContext) }
    assertEquals(listOf(graphQLFixture.tempDir.name, "test.graphql", *expectedItems), items)
  }
}
