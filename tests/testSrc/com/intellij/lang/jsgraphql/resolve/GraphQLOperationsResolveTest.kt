package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLOperationsResolveTest : GraphQLResolveTestCaseBase() {

  companion object {
    private const val GITHUB_SCHEMA = "GithubSchema.graphql"
  }

  override fun getBasePath(): String {
    return "/resolve/operations"
  }

  fun testQueryFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  fun testCustomQueryFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  fun testSelectionSetQueryFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  fun testMutationFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createUser")
  }

  fun testCustomMutationFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createUser")
  }

  fun testSubscriptionFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "users")
  }

  fun testCustomSubscriptionFieldRoot() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "users")
  }

  fun testFragmentName() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFragmentDefinition::class.java, "fragment1")
  }

  fun testFragmentObjectField() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
  }

  fun testFragmentObjectFieldExtension() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "email")
  }

  fun testFragmentInterfaceField() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "id")
  }

  fun testFragmentInterfaceFieldExtension() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createdAt")
  }

  fun testFragmentUnionField() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "language")
  }

  fun testFragmentUnionFieldExtension() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "someOtherField")
  }

  fun testFragmentInlineAnonymous() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "id")
  }

  fun testFieldArgument() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "after")
  }

  fun testDirectiveArgument() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "second")
  }

  fun testDirective() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "someDir")
  }

  fun testInputValue() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "address")
  }

  fun testInputValueNested() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "zip")
  }

  fun testInputValueDefinitionDefault() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "street")
  }

  fun testGithubQueries() = runBlockingCancellable {
    myFixture.copyFileToProject(GITHUB_SCHEMA)
    doHighlightingTest()
  }

  fun testUnresolvedReferences() = runBlockingCancellable {
    myFixture.copyFileToProject(GITHUB_SCHEMA)
    doHighlightingTest()
  }

  fun testFragmentExplicitDocumentsGlob() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "bar/schema.graphql"
    )
  }

  fun testFragmentFallbackToFirstNonStrictProject() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "foo/schema.graphql"
    )
  }

  fun testFragmentFallbackToFirstNonStrictProjectSkipInclude() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "bar/schema.graphql"
    )
  }

  fun testFragmentMatchedBySchema() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "bar/bar.js",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "bar/schema.graphql"
    )
  }

  fun testFragmentInjectedInHtml() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "index.html",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "fragments.graphql"
    )
  }

  fun testFragmentInjectedResolvedToOtherInjection() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "index.html",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "fragments.js"
    )
  }

  fun testFragmentIncluded() = runBlockingCancellable<Unit> {
    doProjectResolveTest(
      "query.graphql",
      GraphQLFragmentDefinition::class.java,
      "UserFragment",
      "fragments.graphql"
    )
  }

  fun testFragmentExcluded() = runBlockingCancellable {
    doProjectHighlighting("query.graphql")
  }

  fun testInputFieldAsVariableDefaultValue() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "direction")
  }
}
