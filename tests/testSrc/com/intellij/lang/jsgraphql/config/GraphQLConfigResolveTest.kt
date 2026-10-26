package com.intellij.lang.jsgraphql.config

import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigContributor
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.ide.config.serialization.GraphQLConfigPrinter
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.lang.jsgraphql.withCustomEnv
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.yaml.YAMLFileType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLConfigResolveTest {
  private val graphQLFixture by graphQLTestFixture("/config/resolve", initTestProject = true)

  @Test
  fun testConfigRc(): Unit = timeoutRunBlocking {
    val config = resolveConfig("dir/schema.graphql")
    assertEquals(".graphqlrc", graphQLFixture.getRelativePath(config.file))
    val fileType = readAction { PsiManager.getInstance(graphQLFixture.project).findFile(config.file!!)?.fileType }
    assertEquals(YAMLFileType.YML, fileType)
    assertConfigContentsMatches(config)
  }

  @Test
  fun testConfigRcAsJson(): Unit = timeoutRunBlocking {
    val config = resolveConfig("dir/schema.graphql")
    assertEquals(".graphqlrc", graphQLFixture.getRelativePath(config.file))
    val fileType = readAction { PsiManager.getInstance(graphQLFixture.project).findFile(config.file!!)?.fileType }
    assertEquals(JsonFileType.INSTANCE, fileType)
    assertConfigContentsMatches(config)
  }

  @Test
  fun testDontSkipEmptyRootConfigs(): Unit = timeoutRunBlocking {
    val config = resolveConfig("some/nested/dir/nested.graphql")
    assertEquals("some/.graphqlrc.yml", graphQLFixture.getRelativePath(config.file))
    assertConfigContentsMatches(config)
  }

  @Test
  fun testSchemaInEnvVariable(): Unit = timeoutRunBlocking {
    val filename = "dir/schema.graphql"

    withCustomEnv(graphQLFixture.project, mapOf("SCHEMA_PATH" to filename)) {
      val config = resolveConfig(filename)
      assertEquals("graphql.config.yml", graphQLFixture.getRelativePath(config.file))
      assertEquals(filename, config.schema.first().filePath)
      assertConfigContentsMatches(config)
    }
  }

  @Test
  fun testGlobAsPath(): Unit = timeoutRunBlocking {
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getAllConfigs().first()
    val pointer = config.getDefault()?.schema?.first()!!
    assertEquals("src/**/*.graphql", pointer.globPath)
    assertEquals(null, pointer.filePath)
  }

  @Test
  fun testInjection(): Unit = timeoutRunBlocking {
    val config = resolveConfig("dir/file.js")
    assertEquals(".graphqlrc.yml", graphQLFixture.getRelativePath(config.file))
  }

  @Test
  fun testJsonSchema(): Unit = timeoutRunBlocking {
    val config = resolveConfig("dir/remoteSchema.json")
    assertEquals(".graphqlrc.yml", graphQLFixture.getRelativePath(config.file))

    noConfig("dir2/otherSchema.json")
  }

  @Test
  fun testOverriddenScope(): Unit = timeoutRunBlocking {
    GraphQLConfigContributor.EP_NAME.point.registerExtension(object : GraphQLConfigContributor {
      override fun contributeConfigs(project: Project): Collection<GraphQLConfig> {
        return listOf(
          GraphQLConfig(
            project,
            graphQLFixture.fixture.findFileInTempDir("main/graphql/servicea"),
            null,
            GraphQLRawConfig.EMPTY,
          ),
          GraphQLConfig(
            project,
            graphQLFixture.fixture.findFileInTempDir("main/graphql/serviceb"),
            null,
            GraphQLRawConfig.EMPTY,
          ),
          GraphQLConfig(
            project,
            graphQLFixture.fixture.findFileInTempDir("main/graphql/servicec/schema"),
            null,
            GraphQLRawConfig.EMPTY,
          ),
        )
      }
    }, graphQLFixture.fixture.testRootDisposable)
    graphQLFixture.fixture.reloadGraphQLConfiguration()

    val configA = resolveConfig("main/graphql/servicea/operations.graphql")
    assertEquals("main/graphql/servicea", graphQLFixture.getRelativePath(configA.dir))

    val configB = resolveConfig("main/graphql/serviceb/operations.graphql")
    assertEquals("main/graphql/serviceb", graphQLFixture.getRelativePath(configB.dir))

    // a physical config file should take precedence over a contributed one
    val configC = resolveConfig("main/graphql/servicec/schema/operations.graphql")
    assertEquals("main/graphql/servicec", graphQLFixture.getRelativePath(configC.dir))
    assertEquals("main/graphql/servicec/graphql.config.yml", graphQLFixture.getRelativePath(configC.file))
  }

  @Test
  fun testGlobStartingWithStar(): Unit = timeoutRunBlocking {
    assertConfigContentsMatches(
      GraphQLConfigProvider.getInstance(graphQLFixture.project)
        .getForConfigFile(graphQLFixture.fixture.findFileInTempDir("graphql.config.yml")!!)!!
    )
  }

  @Test
  fun testComplexConfig(): Unit = timeoutRunBlocking {
    assertConfigContentsMatches(
      GraphQLConfigProvider.getInstance(graphQLFixture.project)
        .getForConfigFile(graphQLFixture.fixture.findFileInTempDir("graphql.config.yml")!!)!!
    )
  }

  @Test
  fun testGatsby(): Unit = timeoutRunBlocking {
    assertEquals("graphql.config.js", graphQLFixture.getRelativePath(resolveConfig(".cache/typegen/schema.graphql").file))
    assertEquals("graphql.config.js", graphQLFixture.getRelativePath(resolveConfig("src/query.js").file))
  }

  private suspend fun resolveConfig(filePath: String): GraphQLProjectConfig {
    val context = graphQLFixture.fixture.configureFromTempProjectFile(filePath)
    assertNotNull(context, "source file is not found")
    val config = smartReadAction(graphQLFixture.project) {
      GraphQLConfigProvider.getInstance(graphQLFixture.project).resolveProjectConfig(context)
    }
    assertNotNull(config, "config is not resolved")
    return config!!
  }

  private fun noConfig(filePath: String) {
    val context = graphQLFixture.fixture.configureFromTempProjectFile(filePath)
    assertNotNull(context, "source file is not found")
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).resolveProjectConfig(context)
    assertNull(config, "config should be null")
  }

  private suspend fun assertConfigContentsMatches(config: GraphQLProjectConfig) {
    assertConfigContentsMatches(config.rootConfig)
  }

  private suspend fun assertConfigContentsMatches(config: GraphQLConfig) {
    val text = GraphQLConfigPrinter.toYml(config.rawData)!!
    val file = readAction {
      PsiFileFactory.getInstance(graphQLFixture.project).createFileFromText("graphql.config.yml", YAMLFileType.YML, text)
    }
    WriteCommandAction.runWriteCommandAction(graphQLFixture.project) {
      CodeStyleManager.getInstance(graphQLFixture.project).reformat(file)
    }
    graphQLFixture.fixture.configureByText("config.content.yml", file.text)
    graphQLFixture.fixture.checkResultByFile("${graphQLFixture.getTestName(true)}_expected.yml")
  }
}
