package com.intellij.lang.jsgraphql.config

import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection
import com.intellij.lang.jsgraphql.getTestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class GraphQLConfigImplicitUsageProviderTest: BasePlatformTestCase() {

  override fun getTestDataPath(): String = getTestDataPath(basePath) + "/config/implicitUsages"

  override fun setUp() {
    super.setUp()
    myFixture.enableInspections(JSUnusedGlobalSymbolsInspection::class.java)
  }

  fun testConfigImplicitUsage() {
    myFixture.testHighlighting(true, false, true, ".graphqlrc.mjs", "graphql.config.js", "graphql.config.ts")
  }

  fun testFalsePositiveImplicitUsage() {
    myFixture.testHighlighting(true, false, true, "graphql.js")
  }
}