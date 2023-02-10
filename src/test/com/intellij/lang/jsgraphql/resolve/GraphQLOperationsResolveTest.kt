package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFragmentDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition

class GraphQLOperationsResolveTest : GraphQLResolveTestCaseBase() {

    companion object {
        private const val GITHUB_SCHEMA = "GithubSchema.graphql"
    }

    override fun getBasePath(): String {
        return "/resolve/operations"
    }

    fun testQueryFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
    }

    fun testCustomQueryFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
    }

    fun testSelectionSetQueryFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
    }

    fun testMutationFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createUser")
    }

    fun testCustomMutationFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createUser")
    }

    fun testSubscriptionFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "users")
    }

    fun testCustomSubscriptionFieldRoot() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "users")
    }

    fun testFragmentName() {
        doResolveWithOffsetTest(GraphQLFragmentDefinition::class.java, "fragment1")
    }

    fun testFragmentObjectField() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "name")
    }

    fun testFragmentObjectFieldExtension() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "email")
    }

    fun testFragmentInterfaceField() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "id")
    }

    fun testFragmentInterfaceFieldExtension() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "createdAt")
    }

    fun testFragmentUnionField() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "language")
    }

    fun testFragmentUnionFieldExtension() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "someOtherField")
    }

    fun testFragmentInlineAnonymous() {
        doResolveWithOffsetTest(GraphQLFieldDefinition::class.java, "id")
    }

    fun testFieldArgument() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "after")
    }

    fun testDirectiveArgument() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "second")
    }

    fun testDirective() {
        doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "someDir")
    }

    fun testInputValue() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "address")
    }

    fun testInputValueNested() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "zip")
    }

    fun testInputValueDefinitionDefault() {
        doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "street")
    }

    fun testGithubQueries() {
        myFixture.copyFileToProject(GITHUB_SCHEMA)
        doHighlightingTest()
    }

    fun testUnresolvedReferences() {
        myFixture.copyFileToProject(GITHUB_SCHEMA)
        doHighlightingTest()
    }

    fun testFragmentExplicitDocumentsGlob() {
        doProjectResolveTest(
            "bar/bar.js",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "bar/schema.graphql"
        )
    }

    fun testFragmentFallbackToFirstNonStrictProject() {
        doProjectResolveTest(
            "bar/bar.js",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "foo/schema.graphql"
        )
    }

    fun testFragmentFallbackToFirstNonStrictProjectSkipInclude() {
        doProjectResolveTest(
            "bar/bar.js",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "bar/schema.graphql"
        )
    }

    fun testFragmentMatchedBySchema() {
        doProjectResolveTest(
            "bar/bar.js",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "bar/schema.graphql"
        )
    }

    fun testFragmentInjectedInHtml() {
        doProjectResolveTest(
            "index.html",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "fragments.graphql"
        )
    }

    fun testFragmentInjectedResolvedToOtherInjection() {
        doProjectResolveTest(
            "index.html",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "fragments.js"
        )
    }

    fun testFragmentIncluded() {
        doProjectResolveTest(
            "query.graphql",
            GraphQLFragmentDefinition::class.java,
            "UserFragment",
            "fragments.graphql"
        )
    }

    fun testFragmentExcluded() {
        doProjectHighlighting("query.graphql")
    }
}
