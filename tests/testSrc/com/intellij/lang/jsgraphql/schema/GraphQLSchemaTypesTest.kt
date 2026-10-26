/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.openapi.application.smartReadAction
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLSchemaTypesTest {
  private val graphQLFixture by graphQLTestFixture("/schema/types", enableAllInspections = true, initTestProject = true)

  @Test
  fun testMultipleSchemasLegacy(): Unit = timeoutRunBlocking {
    // an explicit test for empty schema config
    doTest(
      "schema-one/query-one.graphql",
      listOf("fieldOne", "__typename"),
      listOf("FragOne1", "FragOne2"),
      listOf("Query", "SchemaOneType", "SchemaOneAdditional"),
    )

    // explicit includes and excludes
    doTest(
      "schema-two/query-two.graphql",
      listOf("fieldTwo", "__typename"),
      listOf("FragTwoIncluded"),
      listOf("Query", "TwoIncludedType", "TwoAdditionalType")
    )

    // only `schemaPath` includes all documents implicitly
    doTest(
      "schema-three/query1.graphql",
      listOf("one", "two", "__typename"),
      listOf("ThreeOneFragment", "ThreeTwoFragment", "ThreeImplicitlyIncludedFragment"),
      listOf("Query", "Three1", "Three2")
    )

    // existing `includes` enables "strict" mode, so only matching types and documents are used
    doTest(
      "schema-four/query.graphql",
      listOf("four", "additional", "__typename"),
      listOf("FourFragment", "FourFragment1"),
      listOf("Query", "FourType", "FourType1")
    )
  }

  @Test
  fun testExcludeLegacy(): Unit = timeoutRunBlocking {
    val fileName = "Types3.graphql"
    doTestTypeDefinitions(fileName, listOf("TheOnlyType"))
    doTestFragmentDefinitions(fileName, listOf("TheOnlyFragment"))
  }

  @Test
  fun testFragmentsInInjections(): Unit = timeoutRunBlocking {
    doTest(
      "src/query2.js",
      listOf("FragmentOne", "FragmentTwo", "FragmentThree", "FragmentFour", "FragmentInSchema", "FragmentTested", "on"),
      listOf("FragmentOne", "FragmentTwo", "FragmentThree", "FragmentFour", "FragmentInSchema", "FragmentTested"),
      listOf("Query", "User")
    )

    doTestHighlighting("src/query1.js", "src/index.html")
  }

  @Test
  fun testSchemaInJson(): Unit = timeoutRunBlocking {
    val fileName = "client/query.graphql"
    doTestCompletion(fileName, listOf("localField", "Activity", "Character", "GenreCollection"), false)
    doTestTypeDefinitions(fileName, listOf("LocalType", "ThreadCommentLikeNotification", "ActivityMessageNotification", "ModAction"), false)
    doTestHighlighting("client/highlight.graphql")
  }

  @Test
  fun testSchemaInHtmlWithSOE(): Unit = timeoutRunBlocking {
    val fileName = "schema.graphql"
    doTestFragmentDefinitions(fileName, listOf("UserFragment"))
    doTestTypeDefinitions(fileName, listOf("User", "Query"))
  }

  private suspend fun doTest(
    fileName: String,
    expectedCompletions: List<String>,
    expectedFragments: List<String>,
    expectedTypes: List<String>,
    strict: Boolean = true,
  ) {
    doTestCompletion(fileName, expectedCompletions, strict)
    doTestFragmentDefinitions(fileName, expectedFragments, strict)
    doTestTypeDefinitions(fileName, expectedTypes, strict)
  }

  private fun doTestCompletion(fileName: String, expectedCompletions: List<String>, strict: Boolean = true) {
    graphQLFixture.fixture.configureFromTempProjectFile(fileName)
    graphQLFixture.fixture.complete(CompletionType.BASIC, 1)
    val completions = graphQLFixture.fixture.lookupElementStrings ?: emptyList()
    if (strict) {
      assertThat(completions).hasSameElementsAs(expectedCompletions)
    }
    else {
      assertThat(completions).containsAll(expectedCompletions)
    }
  }

  private suspend fun doTestFragmentDefinitions(fileName: String, expectedFragments: List<String>, strict: Boolean = true) {
    val file = graphQLFixture.fixture.configureFromTempProjectFile(fileName)!!
    val fragments = smartReadAction(graphQLFixture.project) {
      GraphQLPsiSearchHelper.getInstance(graphQLFixture.project).findFragmentDefinitions(file).map { it.name }
    }
    if (strict) {
      assertThat(fragments).hasSameElementsAs(expectedFragments)
    }
    else {
      assertThat(fragments).containsAll(expectedFragments)
    }
  }

  private suspend fun doTestTypeDefinitions(fileName: String, expectedTypes: List<String>, strict: Boolean = true) {
    val psiFile = graphQLFixture.fixture.configureFromTempProjectFile(fileName)
    assertNotNull(psiFile)
    val registry = smartReadAction(graphQLFixture.project) {
      GraphQLSchemaProvider.getInstance(graphQLFixture.project).getSchemaInfo(psiFile).registry
    }
    val types = registry.types().values
      .map { it.name }
      .filter { !GraphQLKnownTypes.isIntrospectionType(it) }
    if (strict) {
      assertThat(types).hasSameElementsAs(expectedTypes)
    }
    else {
      assertThat(types).containsAll(expectedTypes)
    }
  }

  private fun doTestHighlighting(vararg files: String) {
    files.forEach {
      graphQLFixture.fixture.configureFromTempProjectFile(it)
      graphQLFixture.fixture.checkHighlighting()
    }
  }
}
