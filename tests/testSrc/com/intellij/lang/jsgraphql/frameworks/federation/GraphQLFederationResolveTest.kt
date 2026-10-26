package com.intellij.lang.jsgraphql.frameworks.federation

import com.intellij.lang.jsgraphql.graphQLResolveTestFixture
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeExtensionDefinition
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes
import com.intellij.lang.jsgraphql.withLibrary
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLFederationResolveTest {
  private val resolveFixture by graphQLResolveTestFixture("/frameworks/federation/resolve")

  @Test
  fun testQueryExtensionsService(): Unit = timeoutRunBlocking {
    withLibrary(resolveFixture.project, GraphQLBundledLibraryTypes.FEDERATION) {
      val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "_service")
      resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeExtensionDefinition::class.java, "Query")
    }
  }

  @Test
  fun testQueryExtensionsServiceField(): Unit = timeoutRunBlocking {
    withLibrary(resolveFixture.project, GraphQLBundledLibraryTypes.FEDERATION) {
      val target =  resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "sdl")
      resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "_Service")
    }
  }

  @Test
  fun testQueryExtensionsOnlyOnRootLevel(): Unit = timeoutRunBlocking {
    withLibrary(resolveFixture.project, GraphQLBundledLibraryTypes.FEDERATION) { resolveFixture.doHighlightingTest() }
  }

  @Test
  fun testNotResolvedIfDisabled(): Unit = timeoutRunBlocking {
    resolveFixture.doHighlightingTest()
  }
}
