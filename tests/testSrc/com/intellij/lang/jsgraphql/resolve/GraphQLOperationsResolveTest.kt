package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.graphQLResolveTestFixture
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLOperationsResolveTest {
  companion object {
    private const val GITHUB_SCHEMA = "GithubSchema.graphql"
  }

  private val resolveFixture by graphQLResolveTestFixture("/resolve/operations")

  @Test
  fun testQueryFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  @Test
  fun testCustomQueryFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  @Test
  fun testSelectionSetQueryFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  @Test
  fun testMutationFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createUser")
  }

  @Test
  fun testCustomMutationFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createUser")
  }

  @Test
  fun testSubscriptionFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "users")
  }

  @Test
  fun testCustomSubscriptionFieldRoot(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "users")
  }

  @Test
  fun testFragmentName(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFragmentDefinition::class.java, "fragment1")
  }

  @Test
  fun testFragmentObjectField(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  @Test
  fun testFragmentObjectFieldExtension(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "email")
  }

  @Test
  fun testFragmentInterfaceField(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "id")
  }

  @Test
  fun testFragmentInterfaceFieldExtension(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createdAt")
  }

  @Test
  fun testFragmentUnionField(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "language")
  }

  @Test
  fun testFragmentUnionFieldExtension(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "someOtherField")
  }

  @Test
  fun testFragmentInlineAnonymous(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "id")
  }

  @Test
  fun testFieldArgument(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "after")
  }

  @Test
  fun testDirectiveArgument(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "second")
  }

  @Test
  fun testDirective(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "someDir")
  }

  @Test
  fun testInputValue(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "address")
  }

  @Test
  fun testInputValueNested(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "zip")
  }

  @Test
  fun testInputValueDefinitionDefault(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "street")
  }

  @Test
  fun testGithubQueries(): Unit = timeoutRunBlocking {
    resolveFixture.fixture.copyFileToProject(GITHUB_SCHEMA)
    resolveFixture.doHighlightingTest()
  }

  @Test
  fun testUnresolvedReferences(): Unit = timeoutRunBlocking {
    resolveFixture.fixture.copyFileToProject(GITHUB_SCHEMA)
    resolveFixture.doHighlightingTest()
  }

  @Test
  fun testFragmentExplicitDocumentsGlob(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "bar/schema.graphql"
    )
  }

  @Test
  fun testFragmentFallbackToFirstNonStrictProject(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "foo/schema.graphql"
    )
  }

  @Test
  fun testFragmentFallbackToFirstNonStrictProjectSkipInclude(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "bar/schema.graphql"
    )
  }

  @Test
  fun testFragmentMatchedBySchema(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "bar/schema.graphql"
    )
  }

  @Test
  fun testFragmentInjectedInHtml(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "index.html",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "fragments.graphql"
    )
  }

  @Test
  fun testFragmentInjectedResolvedToOtherInjection(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "index.html",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "fragments.js"
    )
  }

  @Test
  fun testFragmentIncluded(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectResolveTest(
      "query.graphql",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "fragments.graphql"
    )
  }

  @Test
  fun testFragmentExcluded(): Unit = timeoutRunBlocking {
    resolveFixture.doProjectHighlighting("query.graphql")
  }

  @Test
  fun testInputFieldAsVariableDefaultValue(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "direction")
  }
}
