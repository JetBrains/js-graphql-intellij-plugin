package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GRAPHQLCONFIG
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.lang.jsgraphql.ide.config.migration.GraphQLMigrateLegacyConfigAction
import com.intellij.openapi.application.EDT
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLConfigMigrationTest {
  private val graphQLFixture by graphQLTestFixture("/config/migration")

  @Test
  fun testMigrateLegacyConfig(): Unit = timeoutRunBlocking {
    withContext(Dispatchers.EDT) {
      graphQLFixture.fixture.copyDirectoryToProject(graphQLFixture.getTestName(true), "")
    }
    graphQLFixture.fixture.configureFromTempProjectFile(GRAPHQLCONFIG)
    graphQLFixture.fixture.performEditorAction(GraphQLMigrateLegacyConfigAction.ACTION_ID)
    val expectedFile = "${graphQLFixture.getTestName(true)}_expected.yml"
    graphQLFixture.fixture.checkResultByFile(GraphQLConfigFactory.PREFERRED_CONFIG, expectedFile, true)
  }
}
