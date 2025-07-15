/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLSchemaTypesTest : GraphQLTestCaseBase() {
  override fun getBasePath() = "/schema/types"

  override fun setUp() {
    super.setUp()

    runBlockingCancellable {
      initTestProject()
      enableAllInspections()
    }
  }

  fun testMultipleSchemasLegacy() = runBlockingCancellable {
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

  fun testExcludeLegacy() = runBlockingCancellable {
    val fileName = "Types3.graphql"
    doTestTypeDefinitions(fileName, listOf("TheOnlyType"))
    doTestFragmentDefinitions(fileName, listOf("TheOnlyFragment"))
  }

  fun testFragmentsInInjections() = runBlockingCancellable {
    doTest(
      "src/query2.js",
      listOf("FragmentOne", "FragmentTwo", "FragmentThree", "FragmentFour", "FragmentInSchema", "FragmentTested", "on"),
      listOf("FragmentOne", "FragmentTwo", "FragmentThree", "FragmentFour", "FragmentInSchema", "FragmentTested"),
      listOf("Query", "User")
    )

    doTestHighlighting("src/query1.js", "src/index.html")
  }

  fun testSchemaInJson() = runBlockingCancellable {
    val fileName = "client/query.graphql"
    doTestCompletion(fileName, listOf("localField", "Activity", "Character", "GenreCollection"), false)
    doTestTypeDefinitions(fileName, listOf("LocalType", "ThreadCommentLikeNotification", "ActivityMessageNotification", "ModAction"), false)
    doTestHighlighting("client/highlight.graphql")
  }

  fun testSchemaInHtmlWithSOE() = runBlockingCancellable {
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
    myFixture.configureFromTempProjectFile(fileName)
    myFixture.complete(CompletionType.BASIC, 1)
    val completions = myFixture.lookupElementStrings ?: emptyList()
    if (strict) {
      assertSameElements(completions, expectedCompletions)
    }
    else {
      assertContainsElements(completions, expectedCompletions)
    }
  }

  private suspend fun doTestFragmentDefinitions(fileName: String, expectedFragments: List<String>, strict: Boolean = true) {
    val file = myFixture.configureFromTempProjectFile(fileName)!!
    val fragments = smartReadAction(project) { GraphQLPsiSearchHelper.getInstance(project).findFragmentDefinitions(file).map { it.name } }
    if (strict) {
      assertSameElements(fragments, expectedFragments)
    }
    else {
      assertContainsElements(fragments, expectedFragments)
    }
  }

  private suspend fun doTestTypeDefinitions(fileName: String, expectedTypes: List<String>, strict: Boolean = true) {
    val psiFile = myFixture.configureFromTempProjectFile(fileName)
    assertNotNull(psiFile)
    val registry = smartReadAction(project) { GraphQLSchemaProvider.getInstance(project).getSchemaInfo(psiFile).registry }
    val types = registry.types().values
      .map { it.name }
      .filter { !GraphQLKnownTypes.isIntrospectionType(it) }
    if (strict) {
      assertSameElements(types, expectedTypes)
    }
    else {
      assertContainsElements(types, expectedTypes)
    }
  }

  private fun doTestHighlighting(vararg files: String) {
    files.forEach {
      myFixture.configureFromTempProjectFile(it)
      myFixture.checkHighlighting()
    }
  }
}
