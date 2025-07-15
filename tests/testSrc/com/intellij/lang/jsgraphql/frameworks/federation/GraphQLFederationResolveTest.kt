package com.intellij.lang.jsgraphql.frameworks.federation

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeExtensionDefinition
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes
import com.intellij.lang.jsgraphql.withLibrary
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLFederationResolveTest : GraphQLResolveTestCaseBase() {
  override fun getBasePath(): String {
    return "/frameworks/federation/resolve"
  }

  fun testQueryExtensionsService() = runBlockingCancellable {
    withLibrary(project, GraphQLBundledLibraryTypes.FEDERATION) {
      val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "_service")
      assertContainingDefinition(target, GraphQLObjectTypeExtensionDefinition::class.java, "Query")
    }
  }

  fun testQueryExtensionsServiceField() = runBlockingCancellable {
    withLibrary(project, GraphQLBundledLibraryTypes.FEDERATION) {
      val target =  doResolveAsTextTest(GraphQLFieldDefinition::class.java, "sdl")
      assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "_Service")
    }
  }

  fun testQueryExtensionsOnlyOnRootLevel() = runBlockingCancellable {
    withLibrary(project, GraphQLBundledLibraryTypes.FEDERATION) { doHighlightingTest() }
  }

  fun testNotResolvedIfDisabled() = runBlockingCancellable {
    doHighlightingTest()
  }
}
