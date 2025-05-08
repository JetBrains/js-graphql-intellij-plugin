package com.intellij.lang.jsgraphql.frameworks.federation

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryTypes
import com.intellij.lang.jsgraphql.withLibrary

class GraphQLFederationValidationTest : GraphQLTestCaseBase() {

  @Throws(Exception::class)
  override fun setUp() {
    super.setUp()
    enableAllInspections()
  }

  override fun getBasePath() = "/frameworks/federation/validation"

  fun testQueryValidation() {
    withLibrary(project, GraphQLLibraryTypes.FEDERATION, {
      doHighlightingTest()

      val schemaInfo = GraphQLSchemaProvider.getInstance(project).getSchemaInfo(myFixture.file)
      assertNotNull(schemaInfo)
      assertEmpty(schemaInfo.getErrors(project))
    }, testRootDisposable)
  }

  fun testEmptySchemaValidation() {
    withLibrary(project, GraphQLLibraryTypes.FEDERATION, {
      val globalScope = GraphQLScopeProvider.getInstance(project).globalScope
      val schemaInfo = GraphQLSchemaProvider.getInstance(project).getSchemaInfo(globalScope)
      assertNotNull(schemaInfo)
      assertEmpty(schemaInfo.getErrors(project))
    }, testRootDisposable)
  }
}
