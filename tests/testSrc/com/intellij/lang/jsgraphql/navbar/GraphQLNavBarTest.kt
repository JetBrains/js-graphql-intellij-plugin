package com.intellij.lang.jsgraphql.navbar

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.platform.navbar.testFramework.contextNavBarPathStrings
import org.intellij.lang.annotations.Language

class GraphQLNavBarTest : GraphQLTestCaseBase() {

  fun testNamedQuery() = doTest(
    """
      query MyQuery {
        us<caret>er
      }
    """.trimIndent(),
    "MyQuery", "user"
  )

  fun testAnonymousQuery() = doTest(
    """
      {
        us<caret>er
      }
    """.trimIndent(),
    "anonymous query", "user"
  )

  fun testNestedField() = doTest(
    """
      query MyQuery {
        user {
          na<caret>me
        }
      }
    """.trimIndent(),
    "MyQuery", "user", "name"
  )

  fun testFragmentDefinition() = doTest(
    """
      fragment MyFragment on User {
        na<caret>me
      }
    """.trimIndent(),
    "MyFragment", "name"
  )

  fun testInlineFragment() = doTest(
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

  fun testObjectTypeDefinition() = doTest(
    """
      type User {
        na<caret>me: String
      }
    """.trimIndent(),
    "User", "name"
  )

  fun testFieldArgumentDefinition() = doTest(
    """
      type Query {
        user(i<caret>d: ID): User
      }
    """.trimIndent(),
    "Query", "user", "id"
  )

  fun testEnumTypeDefinition() = doTest(
    """
      enum Color {
        RE<caret>D
        GREEN
      }
    """.trimIndent(),
    "Color", "RED"
  )

  fun testFileRoot() = doTest(
    """
      query MyQuery {
        user
      }
      <caret>
    """.trimIndent()
  )

  private fun doTest(@Language("GraphQL") text: String, vararg expectedItems: String) = runBlockingCancellable {
    myFixture.configureByText("test.graphql", text)
    val dataContext = (myFixture.editor as EditorEx).dataContext
    val items = readAction { contextNavBarPathStrings(dataContext) }
    assertOrderedEquals(items, "src", "test.graphql", *expectedItems)
  }
}
