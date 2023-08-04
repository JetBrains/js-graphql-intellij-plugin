package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GRAPHQLCONFIG
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigFactory
import com.intellij.lang.jsgraphql.ide.config.migration.GraphQLMigrateLegacyConfigAction

class GraphQLConfigMigrationTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/config/migration"

  fun testMigrateLegacyConfig() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    myFixture.configureFromTempProjectFile(GRAPHQLCONFIG)
    myFixture.performEditorAction(GraphQLMigrateLegacyConfigAction.ACTION_ID)
    myFixture.checkResultByFile(GraphQLConfigFactory.PREFERRED_CONFIG, "${getTestName(true)}_expected.yml", true)
  }
}
