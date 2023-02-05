package com.intellij.lang.jsgraphql.config

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.withCustomEnv
import com.intellij.psi.PsiManager
import junit.framework.TestCase
import org.jetbrains.yaml.YAMLFileType

class GraphQLConfigResolveTest : GraphQLTestCaseBase() {

    override fun getBasePath(): String = "/config/resolve"

    fun testConfigRc() {
        val config = doTestResolveProjectConfig("dir/schema.graphql")
        TestCase.assertEquals(".graphqlrc", config.file?.name)
        TestCase.assertEquals(YAMLFileType.YML, PsiManager.getInstance(project).findFile(config.file!!)?.fileType)
    }

    fun testConfigRcAsJson() {
        val config = doTestResolveProjectConfig("dir/schema.graphql")
        TestCase.assertEquals(".graphqlrc", config.file?.name)
        TestCase.assertEquals(JsonFileType.INSTANCE, PsiManager.getInstance(project).findFile(config.file!!)?.fileType)
    }

    fun testSkipEmptyFiles() {
        val config = doTestResolveProjectConfig("some/nested/dir/nested.graphql")
        TestCase.assertEquals("graphql.config.yml", config.file?.name)
    }

    fun testSchemaInEnvVariable() {
        val filename = "dir/schema.graphql"

        withCustomEnv(mapOf("SCHEMA_PATH" to filename)) {
            val config = doTestResolveProjectConfig(filename)
            TestCase.assertEquals("graphql.config.yml", config.file?.name)
            TestCase.assertEquals(filename, config.schema.first().filePath)
        }
    }

    private fun doTestResolveConfig(filePath: String): GraphQLConfig {
        myFixture.copyDirectoryToProject(getTestName(true), "/")
        reloadConfiguration()
        val context = myFixture.configureFromTempProjectFile(filePath)
        TestCase.assertNotNull(context)
        val config = GraphQLConfigProvider.getInstance(project).resolveConfig(context)
        TestCase.assertNotNull(config)
        return config!!
    }

    private fun doTestResolveProjectConfig(filePath: String): GraphQLProjectConfig {
        val config = doTestResolveConfig(filePath)
        val psiFile = myFixture.file
        TestCase.assertNotNull(psiFile)
        val projectConfig = config.matchProject(psiFile!!)
        TestCase.assertNotNull(projectConfig)
        return projectConfig!!
    }
}
