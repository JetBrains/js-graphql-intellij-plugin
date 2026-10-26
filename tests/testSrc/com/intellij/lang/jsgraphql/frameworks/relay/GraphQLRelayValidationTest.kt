package com.intellij.lang.jsgraphql.frameworks.relay

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes
import com.intellij.lang.jsgraphql.withLibrary
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLRelayValidationTest {
  private val graphQLFixture by graphQLTestFixture("/frameworks/relay/validation")

  @Test
  fun testSuppressedInspections(): Unit = timeoutRunBlocking {
    withLibrary(graphQLFixture.project, GraphQLBundledLibraryTypes.RELAY) { graphQLFixture.doHighlightingTest() }
  }
}
