package com.intellij.lang.jsgraphql.config

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.withCustomEnv
import com.intellij.psi.PsiManager
import junit.framework.TestCase
import org.jetbrains.yaml.YAMLFileType

class GraphQLConfigResolveTest : GraphQLTestCaseBase() {

    override fun getBasePath(): String = "/config/resolve"

    override fun setUp() {
        super.setUp()

        copyProject()
    }

    fun testConfigRc() {
        val config = resolveConfig("dir/schema.graphql")
        TestCase.assertEquals("/src/.graphqlrc", config.file?.path)
        TestCase.assertEquals(YAMLFileType.YML, PsiManager.getInstance(project).findFile(config.file!!)?.fileType)
    }

    fun testConfigRcAsJson() {
        val config = resolveConfig("dir/schema.graphql")
        TestCase.assertEquals("/src/.graphqlrc", config.file?.path)
        TestCase.assertEquals(JsonFileType.INSTANCE, PsiManager.getInstance(project).findFile(config.file!!)?.fileType)
    }

    fun testDontSkipEmptyRootConfigs() {
        val config = resolveConfig("some/nested/dir/nested.graphql")
        TestCase.assertEquals("/src/some/.graphqlrc.yml", config.file?.path)
    }

    fun testSchemaInEnvVariable() {
        val filename = "dir/schema.graphql"

        withCustomEnv(mapOf("SCHEMA_PATH" to filename)) {
            val config = resolveConfig(filename)
            TestCase.assertEquals("/src/graphql.config.yml", config.file?.path)
            TestCase.assertEquals(filename, config.schema.first().filePath)
        }
    }

    fun testGlobAsPath() {
        val config = GraphQLConfigProvider.getInstance(project).getAllConfigs().first()
        val pointer = config.getDefault()?.schema?.first()!!
        TestCase.assertEquals("src/**/*.graphql", pointer.globPath)
        TestCase.assertEquals(null, pointer.filePath)
    }

    fun testInjection() {
        val config = resolveConfig("dir/file.js")
        TestCase.assertEquals("/src/.graphqlrc.yml", config.file?.path)
    }

    private fun copyProject() {
        myFixture.copyDirectoryToProject(getTestName(true), "")
        reloadConfiguration()
    }

    private fun resolveConfig(filePath: String): GraphQLProjectConfig {
        val context = myFixture.configureFromTempProjectFile(filePath)
        TestCase.assertNotNull("source file is not found", context)
        val config = GraphQLConfigProvider.getInstance(project).resolveConfig(context)
        TestCase.assertNotNull("config is not resolved", config)
        return config!!
    }
}
