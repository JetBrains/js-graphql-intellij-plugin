package com.intellij.lang.jsgraphql.config

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigContributor
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.config.serialization.GraphQLConfigSerializer
import com.intellij.lang.jsgraphql.withCustomEnv
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
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
        compareContents(config)
    }

    fun testConfigRcAsJson() {
        val config = resolveConfig("dir/schema.graphql")
        TestCase.assertEquals("/src/.graphqlrc", config.file?.path)
        TestCase.assertEquals(JsonFileType.INSTANCE, PsiManager.getInstance(project).findFile(config.file!!)?.fileType)
        compareContents(config)
    }

    fun testDontSkipEmptyRootConfigs() {
        val config = resolveConfig("some/nested/dir/nested.graphql")
        TestCase.assertEquals("/src/some/.graphqlrc.yml", config.file?.path)
        compareContents(config)
    }

    fun testSchemaInEnvVariable() {
        val filename = "dir/schema.graphql"

        withCustomEnv(mapOf("SCHEMA_PATH" to filename)) {
            val config = resolveConfig(filename)
            TestCase.assertEquals("/src/graphql.config.yml", config.file?.path)
            TestCase.assertEquals(filename, config.schema.first().filePath)
            compareContents(config)
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

    fun testJsonSchema() {
        val config = resolveConfig("dir/remoteSchema.json")
        TestCase.assertEquals("/src/.graphqlrc.yml", config.file?.path)

        noConfig("dir2/otherSchema.json")
    }

    fun testOverriddenScope() {
        GraphQLConfigContributor.EP_NAME.point.registerExtension(object : GraphQLConfigContributor {
            override fun contributeConfigs(project: Project): Collection<GraphQLConfig> {
                return listOf(
                    GraphQLConfig(
                        project,
                        myFixture.findFileInTempDir("main/graphql/servicea"),
                        null,
                        GraphQLRawConfig.EMPTY,
                    ),
                    GraphQLConfig(
                        project,
                        myFixture.findFileInTempDir("main/graphql/serviceb"),
                        null,
                        GraphQLRawConfig.EMPTY,
                    ),
                    GraphQLConfig(
                        project,
                        myFixture.findFileInTempDir("main/graphql/servicec/schema"),
                        null,
                        GraphQLRawConfig.EMPTY,
                    ),
                )
            }
        }, testRootDisposable)
        reloadConfiguration()

        val configA = resolveConfig("main/graphql/servicea/operations.graphql")
        TestCase.assertEquals("/src/main/graphql/servicea", configA.dir.path)

        val configB = resolveConfig("main/graphql/serviceb/operations.graphql")
        TestCase.assertEquals("/src/main/graphql/serviceb", configB.dir.path)

        // a physical config file should take precedence over a contributed one
        val configC = resolveConfig("main/graphql/servicec/schema/operations.graphql")
        TestCase.assertEquals("/src/main/graphql/servicec", configC.dir.path)
        TestCase.assertEquals("/src/main/graphql/servicec/graphql.config.yml", configC.file?.path)
    }

    fun testGlobStartingWithStar() {
        compareContents(
            GraphQLConfigProvider.getInstance(project)
                .getForConfigFile(myFixture.findFileInTempDir("graphql.config.yml")!!)!!
        )
    }

    fun testComplexConfig() {
        compareContents(
            GraphQLConfigProvider.getInstance(project)
                .getForConfigFile(myFixture.findFileInTempDir("graphql.config.yml")!!)!!
        )
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

    private fun noConfig(filePath: String) {
        val context = myFixture.configureFromTempProjectFile(filePath)
        TestCase.assertNotNull("source file is not found", context)
        val config = GraphQLConfigProvider.getInstance(project).resolveConfig(context)
        TestCase.assertNull("config should be null", config)
    }

    private fun compareContents(config: GraphQLProjectConfig) {
        val rootConfig = config.rootConfig
        TestCase.assertNotNull("root config is not found", rootConfig)
        compareContents(rootConfig!!)
    }

    private fun compareContents(config: GraphQLConfig) {
        val text = GraphQLConfigSerializer.toYml(config)!!
        val file = PsiFileFactory.getInstance(project).createFileFromText("graphql.config.yml", YAMLFileType.YML, text)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(file)
        }
        myFixture.configureByText("config.content.yml", file.text)
        myFixture.checkResultByFile("${getTestName(true)}_expected.yml")
    }
}
