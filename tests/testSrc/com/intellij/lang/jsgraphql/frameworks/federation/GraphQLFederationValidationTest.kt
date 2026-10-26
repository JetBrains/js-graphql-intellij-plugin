package com.intellij.lang.jsgraphql.frameworks.federation

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLScopeProvider
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes
import com.intellij.lang.jsgraphql.withLibrary
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLFederationValidationTest {
  private val graphQLFixture by graphQLTestFixture("/frameworks/federation/validation", enableAllInspections = true)

  @Test
  fun testQueryValidation(): Unit = timeoutRunBlocking {
    withLibrary(graphQLFixture.project, GraphQLBundledLibraryTypes.FEDERATION) {
      graphQLFixture.doHighlightingTest()

      val schemaInfo = GraphQLSchemaProvider.getInstance(graphQLFixture.project).getSchemaInfo(graphQLFixture.fixture.file)
      assertNotNull(schemaInfo)
      assertThat(schemaInfo.getErrors(graphQLFixture.project)).isEmpty()
    }
  }

  @Test
  fun testEmptySchemaValidation(): Unit = timeoutRunBlocking {
    withLibrary(graphQLFixture.project, GraphQLBundledLibraryTypes.FEDERATION) {
      val globalScope = GraphQLScopeProvider.getInstance(graphQLFixture.project).globalScope
      val schemaInfo = GraphQLSchemaProvider.getInstance(graphQLFixture.project).getSchemaInfo(globalScope)
      assertNotNull(schemaInfo)
      assertThat(schemaInfo.getErrors(graphQLFixture.project)).isEmpty()
    }
  }
}
