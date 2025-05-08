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
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLConfigEnvironment
import com.intellij.lang.jsgraphql.ide.config.env.GraphQLExpandVariableContext
import com.intellij.lang.jsgraphql.ide.config.env.expandVariables
import com.intellij.lang.jsgraphql.ide.config.env.extractEnvironmentVariables
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawEndpoint
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfigEndpoint
import com.intellij.lang.jsgraphql.withCustomEnv
import com.intellij.openapi.project.guessProjectDir
import junit.framework.TestCase
import org.junit.Assert
import java.util.function.Function

class GraphQLConfigEnvironmentTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/config/environment"

  fun testExpandedVariables() {
    withCustomEnv(
      project,
      mapOf(
        "CUSTOM_PATH" to "/user/some/custom/path",
        "AUTH" to "7FGD63HHDY373UFDSJF838FSNDFK3922WSJ99"
      ),
    ) {
      myFixture.copyDirectoryToProject(getTestName(true), "")
      reloadConfiguration()

      val config = GraphQLConfigProvider.getInstance(project).getAllConfigs().first().getDefault()!!
      myFixture.configureByText(JsonFileType.INSTANCE, GraphQLConfigTestPrinter(config).print())
      myFixture.checkResultByFile("${getTestName(true)}_expected.json")

      GraphQLConfigEnvironment.getInstance(project)
        .setExplicitVariable("WITH_DEFAULT", "not/default/anymore/file.graphql", config.file!!)
      reloadConfiguration()

      val updatedConfig = GraphQLConfigProvider.getInstance(project).getAllConfigs().first().getDefault()!!
      myFixture.configureByText(JsonFileType.INSTANCE, GraphQLConfigTestPrinter(updatedConfig).print())
      myFixture.checkResultByFile("${getTestName(true)}_updated_expected.json")
    }
  }

  fun testExpandedVariablesLegacy() {
    withCustomEnv(project, emptyMap()) {
      val dir = project.guessProjectDir()!!

      fun createEndpoint(data: GraphQLRawEndpoint): GraphQLConfigEndpoint {
        val isLegacy = true
        val snapshot = GraphQLConfigEnvironment.getInstance(project)
          .createSnapshot(extractEnvironmentVariables(project, isLegacy, data), null)
        return GraphQLConfigEndpoint(myFixture.project, data, dir, isLegacy, snapshot, null, null)
      }

      val initial = GraphQLRawEndpoint("remoteUrl", "http://localhost/")
      var endpoint = createEndpoint(initial)

      // setup env var resolver
      GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> "$name-value" }

      // verify url with no variables
      Assert.assertEquals("http://localhost/", endpoint.url)

      // add a variable and verify it's expanded
      endpoint = createEndpoint(initial.copy(url = initial.url + "\${env:test-var}"))
      Assert.assertEquals("http://localhost/test-var-value", endpoint.url)

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
      Assert.assertEquals(
        "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-value \${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-value \${test\"}}",
        Gson().toJson(endpoint.headers)
      )

      // verify that as variables change values, the nested objects are expanded
      // this verifies that the values in the original maps are not overwritten as part of the expansion
      GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> "$name-new-value" }
      endpoint = endpoint.withUpdatedEnvironment()
      Assert.assertEquals(
        "{\"boolean\":true,\"number\":3.14,\"auth\":\"$ some value before auth-new-value \${test\",\"nested\":{\"nested-auth\":\"$ some value before auth-new-value \${test\"}}",
        Gson().toJson(endpoint.headers)
      )
    }
  }

  fun testInterpolation() {
    withCustomEnv(project, emptyMap()) {
      val env = mapOf(
        "HOME" to "/usr/bin",
        "PATH" to """C:\Users\my-path"""
      )

      GraphQLConfigEnvironment.getEnvVariable = Function { name: String? -> env[name] }

      fun test(expected: String, value: String, isLegacy: Boolean) {
        val snapshot = GraphQLConfigEnvironment.getInstance(project).createSnapshot(env.keys, null)

        Assert.assertEquals(
          expected,
          expandVariables(
            value,
            GraphQLExpandVariableContext(
              myFixture.project,
              myFixture.project.guessProjectDir()!!,
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

  fun testEnvFile() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    reloadConfiguration()
    val file = myFixture.findFileInTempDir("some/nested/dir/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  fun testEnvFileDuplicatedVars() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    reloadConfiguration()
    val file = myFixture.findFileInTempDir("graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  fun testEnvFileInParent() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    reloadConfiguration()
    val file = myFixture.findFileInTempDir("some/nested/dir/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  fun testEnvFileFallbackToRoot() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    reloadConfiguration()
    val file = myFixture.findFileInTempDir("some/nested/dir/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(file)?.getDefault()!!
    assertEquals("bearer 123456QWERTY", config.endpoints.single().headers["Authorization"])
  }

  fun testEnvPerFile() {
    val root = myFixture.copyDirectoryToProject(getTestName(true), "")
    GraphQLConfigEnvironment.getInstance(project)
      .setExplicitVariable("SCHEMA_URL", "https://google.com/graphql", root)
    reloadConfiguration()

    val childConfig = GraphQLConfigProvider.getInstance(project)
      .getForConfigFile(myFixture.findFileInTempDir("some/dir/graphql.config.yml"))?.getDefault()!!
    TestCase.assertEquals("\${SCHEMA_URL}", childConfig.schema.single().pattern)

    val rootConfig = GraphQLConfigProvider.getInstance(project)
      .getForConfigFile(root.findChild("graphql.config.yml"))?.getDefault()!!
    TestCase.assertEquals("https://google.com/graphql", rootConfig.schema.single().pattern)
  }

  fun testEndpointShouldUpdateSchemaPathOnEnvChange() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    reloadConfiguration()
    val config = GraphQLConfigProvider.getInstance(project).getAllConfigs().single().getDefault()!!
    var endpoint = config.endpoints.single()
    TestCase.assertEquals("https://default.com/graphql", endpoint.url)
    TestCase.assertEquals("https://default.com/graphql", endpoint.schemaPointer?.url)

    withCustomEnv(project, mapOf("URL" to "https://some.com/api/graphql")) {
      TestCase.assertEquals("https://default.com/graphql", endpoint.url)
      TestCase.assertEquals("https://default.com/graphql", endpoint.schemaPointer?.url)

      endpoint = endpoint.withUpdatedEnvironment()
      TestCase.assertEquals("https://some.com/api/graphql", endpoint.url)
      TestCase.assertEquals("https://some.com/api/graphql", endpoint.schemaPointer?.url)
    }
  }
}
