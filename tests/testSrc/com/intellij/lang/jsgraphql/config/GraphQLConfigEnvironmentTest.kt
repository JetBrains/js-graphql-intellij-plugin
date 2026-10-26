/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.config

import com.google.gson.Gson
import com.intellij.json.JsonFileType
import com.intellij.lang.jsgraphql.GraphQLConfigTestPrinter
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.env.extractEnvironmentVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.lang.jsgraphql.withCustomEnv
import com.intellij.openapi.project.guessProjectDir
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.function.Function

@TestApplication
class GraphQLConfigEnvironmentTest {
  private val graphQLFixture by graphQLTestFixture("/config/environment")

  @Test
  fun testExpandedVariables(): Unit = timeoutRunBlocking {
    withCustomEnv(
      graphQLFixture.project,
      mapOf(
        "CUSTOM_PATH" to "/user/some/custom/path",
        "AUTH" to "7FGD63HHDY373UFDSJF838FSNDFK3922WSJ99"
      ),
    ) {
      graphQLFixture.initTestProject()

      val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getAllConfigs().first().getDefault()!!
      graphQLFixture.fixture.configureByText(JsonFileType.INSTANCE, GraphQLConfigTestPrinter(config, graphQLFixture.tempDir).print())
      graphQLFixture.fixture.checkResultByFile("${graphQLFixture.getTestName(true)}_expected.json")

      GraphQLConfigEnvironment.getInstance(graphQLFixture.project)
        .setExplicitVariable("WITH_DEFAULT", "not/default/anymore/file.graphql", config.file!!)
      graphQLFixture.fixture.reloadGraphQLConfiguration()

      val updatedConfig = GraphQLConfigProvider.getInstance(graphQLFixture.project).getAllConfigs().first().getDefault()!!
      graphQLFixture.fixture.configureByText(JsonFileType.INSTANCE, GraphQLConfigTestPrinter(updatedConfig, graphQLFixture.tempDir).print())
      graphQLFixture.fixture.checkResultByFile("${graphQLFixture.getTestName(true)}_updated_expected.json")
    }
  }

  @Test
  fun testExpandedVariablesLegacy(): Unit = timeoutRunBlocking {
    withCustomEnv(graphQLFixture.project, emptyMap()) {
      val dir = graphQLFixture.project.guessProjectDir()!!

      fun createEndpoint(data: GraphQLRawEndpoint): GraphQLConfigEndpoint {
        val isLegacy = true
        val snapshot = GraphQLConfigEnvironment.getInstance(graphQLFixture.project)
          .createSnapshot(extractEnvironmentVariables(graphQLFixture.project, isLegacy, data), null)
        return GraphQLConfigEndpoint(graphQLFixture.project, data, dir, isLegacy, snapshot, null, null)
      }

      val initial = GraphQLRawEndpoint("remoteUrl", "http://localhost/")
      var endpoint = createEndpoint(initial)

      // setup env var resolver
      GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> "$name-value" }

      // verify url with no variables
      assertEquals("http://localhost/", endpoint.url)

      // add a variable and verify it's expanded
      endpoint = createEndpoint(initial.copy(url = initial.url + "\${env:test-var}"))
      assertEquals("http://localhost/test-var-value", endpoint.url)

      // verify headers as well as nested header objects are expanded
      endpoint = createEndpoint(
        initial.copy(
          headers = mapOf(
            "boolean" to true,
            "number" to 3.14,
            "auth" to "$ some value before \${env:auth} \${test",
            "nested" to mapOf("nested-auth" to "$ some value before \${env:auth} \${test")
          )
        )
      )
      assertEquals(
        "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-value \${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-value \${test\"}}",
        Gson().toJson(endpoint.headers)
      )

      // verify that as variables change values, the nested objects are expanded
      // this verifies that the values in the original maps are not overwritten as part of the expansion
      GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> "$name-new-value" }
      endpoint = endpoint.withUpdatedEnvironment()
      assertEquals(
        "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-new-value \${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-new-value \${test\"}}",
        Gson().toJson(endpoint.headers)
      )
    }
  }

  @Test
  fun testInterpolation(): Unit = timeoutRunBlocking {
    withCustomEnv(graphQLFixture.project, emptyMap()) {
      val env = mapOf(
        "HOME" to "/usr/bin",
        "PATH" to """C:\Users\my-path"""
      )

      GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> env[name] }

      fun test(expected: String, value: String, isLegacy: Boolean) {
        val snapshot = GraphQLConfigEnvironment.getInstance(graphQLFixture.project).createSnapshot(env.keys, null)

        assertEquals(
          expected,
          expandVariables(
            value,
            GraphQLExpandVariableContext(
              graphQLFixture.project,
              graphQLFixture.project.guessProjectDir()!!,
              isLegacy,
              snapshot
            )
          )
        )
      }

      test(
        """text${"$"}{unknown}abc_default_value""",
        """text${"$"}{unknown}abc_${"$"}{WITH_DEFAULT:"default_value"}""",
        false,
      )
      test(
        """https:\\google.com""",
        """${"$"}{URL:"https:\\google.com"}""",
        false,
      )
      test(
        """/usr/bin_C:\Users\my-path_some:default:value""",
        """${"$"}{HOME:./dir/test}_${"$"}{PATH}_${"$"}{DEFAULT:some:default:value}""",
        false,
      )
      test(
        """C:\Users\my-path""",
        """${"$"}{PATH}""",
        false,
      )
      test(
        """C:\Users\my-path""",
        """C:\Users\my-path""",
        false,
      )
      test(
        """""",
        """     """,
        false,
      )
      test(
        """abc""",
        """   abc  """,
        false,
      )

      /* Legacy */

      test(
        """/usr/bin_C:\Users\my-path""",
        """${"$"}{env:HOME}_${"$"}{env:PATH}""",
        true,
      )
    }
  }

  @Test
  fun testEnvFile(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val file = graphQLFixture.fixture.findFileInTempDir("some/nested/dir/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  @Test
  fun testEnvFileDuplicatedVars(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val file = graphQLFixture.fixture.findFileInTempDir("graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  @Test
  fun testEnvFileInParent(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val file = graphQLFixture.fixture.findFileInTempDir("some/nested/dir/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  @Test
  fun testEnvFileFallbackToRoot(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val file = graphQLFixture.fixture.findFileInTempDir("some/nested/dir/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  @Test
  fun testEnvPerFile(): Unit = timeoutRunBlocking {
    val root = graphQLFixture.initTestProject()
    GraphQLConfigEnvironment.getInstance(graphQLFixture.project)
      .setExplicitVariable("SCHEMA_URL", "https://google.com/graphql", root)
    graphQLFixture.fixture.reloadGraphQLConfiguration()

    val childConfig = GraphQLConfigProvider.getInstance(graphQLFixture.project)
      .getForConfigFile(graphQLFixture.fixture.findFileInTempDir("some/dir/graphql.config.yml"))?.getDefault()!!
    assertEquals("\${SCHEMA_URL}", childConfig.schema.single().pattern)

    val rootConfig = GraphQLConfigProvider.getInstance(graphQLFixture.project)
      .getForConfigFile(root.findChild("graphql.config.yml"))?.getDefault()!!
    assertEquals("https://google.com/graphql", rootConfig.schema.single().pattern)
  }

  @Test
  fun testEndpointShouldUpdateSchemaPathOnEnvChange(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getAllConfigs().single().getDefault()!!
    var endpoint = config.endpoints.single()
    assertEquals("https://default.com/graphql", endpoint.url)
    assertEquals("https://default.com/graphql", endpoint.schemaPointer?.url)

    withCustomEnv(graphQLFixture.project, mapOf("URL" to "https://some.com/api/graphql")) {
      assertEquals("https://default.com/graphql", endpoint.url)
      assertEquals("https://default.com/graphql", endpoint.schemaPointer?.url)

      endpoint = endpoint.withUpdatedEnvironment()
      assertEquals("https://some.com/api/graphql", endpoint.url)
      assertEquals("https://some.com/api/graphql", endpoint.schemaPointer?.url)
    }
  }
}
