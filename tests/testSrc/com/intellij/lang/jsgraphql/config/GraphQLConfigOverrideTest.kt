/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLConfigOverridePath
import com.intellij.lang.jsgraphql.createOverrideConfigComment
import com.intellij.lang.jsgraphql.createTestScratchFile
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.lang.jsgraphql.parseOverrideConfigComment
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLConfigOverrideTest {
  private val graphQLFixture by graphQLTestFixture("/config/override", enableAllInspections = true)

  @Test
  fun testOverrideComment() {
    val tests = listOf(
      null to "#    asdsafasf",
      null to "#",
      null to "",
      null to "# .graphqlconfig=!projectName",
      null to "# .graphqlconfig=   !projectName",

      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlconfig",
        null
      ) to "# .graphqlconfig=/user/local/project/.graphqlconfig",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlconfig",
        null
      ) to "# .graphqlconfig=/user/local/project/.graphqlconfig!",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlconfig",
        null
      ) to "# .graphqlconfig=/user/local/project/.graphqlconfig!    ",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlconfig",
        "backend"
      ) to "# .graphqlconfig=/user/local/project/.graphqlconfig!backend",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlconfig",
        "backend"
      ) to "# .graphqlconfig=/user/local/project/.graphqlconfig   !    backend",

      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlrc.yml",
        null
      ) to "# config=/user/local/project/.graphqlrc.yml",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlrc.yml",
        null
      ) to "# config=/user/local/project/.graphqlrc.yml!",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlrc.yml",
        null
      ) to "# config=/user/local/project/.graphqlrc.yml!    ",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlrc.yml",
        "backend"
      ) to "# config=/user/local/project/.graphqlrc.yml!backend",
      GraphQLConfigOverridePath(
        "/user/local/project/.graphqlrc.yml",
        "backend"
      ) to "# config=/user/local/project/.graphqlrc.yml   !   backend",
    )

    tests.forEach {
      assertEquals(it.first, parseOverrideConfigComment(it.second), it.second)
    }
  }

  @Test
  fun testScratch(): Unit = timeoutRunBlocking {
    doScratchTest(
      "child/.graphqlrc.yml",
      GraphQLConfig.DEFAULT_PROJECT,
      createOverrideConfigComment("${graphQLFixture.tempDir.path}/child/.graphqlrc.yml", null),
      """
            fragment InvalidScratchFragment on <error descr="Unknown type \"Node\"">Node</error> {
                <error descr="Unknown field \"name\": The parent selection or operation does not resolve to a valid schema type">name</error>
            }

            fragment ChildNodeFragment on ChildNode {
                name
                childNode {
                    name
                }
            }

            query {
                childNode {
                    childNode {
                        name
                        ...ChildNodeFragment
                        ...ChildNodeFragmentInSchema
                        ...<error descr="Unknown fragment spread \"InvalidNodeFragment\"">InvalidNodeFragment</error>
                    }
                }
            }
            """.trimIndent()
    )
  }

  @Test
  fun testScratchWithProject(): Unit = timeoutRunBlocking {
    doScratchTest(
      ".graphqlrc.yml",
      "frontend",
      createOverrideConfigComment("${graphQLFixture.tempDir.path}/.graphqlrc.yml", "frontend"),
      """
            fragment ScratchFragmentInvalid on <error descr="Unknown type \"Server\"">Server</error> {
                <error descr="Unknown field \"name\": The parent selection or operation does not resolve to a valid schema type">name</error>
            }

            fragment ScratchFragment on Client {
                name
            }

            query {
                client {
                    name
                }
                <error descr="Unknown field \"server\" on object type \"Query\"">server</error>
            }
            """.trimIndent()
    )
  }

  @Test
  fun testScratchFallbackToRoot(): Unit = timeoutRunBlocking {
    doScratchTest(
      ".graphqlrc.yml",
      GraphQLConfig.DEFAULT_PROJECT,
      "",
      """
            fragment InvalidScratchFragment on <error descr="Unknown type \"ChildNode\"">ChildNode</error> {
                <error descr="Unknown field \"name\": The parent selection or operation does not resolve to a valid schema type">name</error>
            }

            fragment ScratchFragment on Node {
                name
            }

            query {
                node {
                    child {
                        name
                        ...NodeFragment
                        ...ScratchFragment
                        ...<error descr="Unknown fragment spread \"ChildNodeFragmentInSchema\"">ChildNodeFragmentInSchema</error>
                    }
                }
            }
            """.trimIndent()
    )
  }

  @Test
  fun testScratchFallbackToRootIfUnknownPathProvided(): Unit = timeoutRunBlocking {
    doScratchTest(
      ".graphqlrc.yml",
      GraphQLConfig.DEFAULT_PROJECT,
      createOverrideConfigComment("${graphQLFixture.tempDir.path}/does/not/exist/.graphqlrc.yml", null),
      """
            query {
                node {
                    child {
                        name
                        ...NodeFragment
                    }
                }
            }
            """.trimIndent()
    )
  }

  private suspend fun doScratchTest(
    expectedConfigPath: String,
    expectedProject: String,
    comment: String,
    query: String,
  ) {
    WorkspaceEntityLifecycleSupporterUtils.ensureAllEntitiesInWorkspaceAreAsProvidersDefined(graphQLFixture.project)
    graphQLFixture.initTestProject()
    val scratchFile = createTestScratchFile(graphQLFixture.fixture, comment, query)!!
    graphQLFixture.fixture.configureFromExistingVirtualFile(scratchFile)

    val projectConfig = resolveConfig(scratchFile)
    assertEquals(expectedConfigPath, graphQLFixture.getRelativePath(projectConfig.file))
    assertEquals(expectedProject, projectConfig.name)

    graphQLFixture.fixture.checkHighlighting()
  }

  private suspend fun resolveConfig(file: VirtualFile): GraphQLProjectConfig {
    return smartReadAction(graphQLFixture.project) {
      val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).resolveProjectConfig(file)
      assertNotNull(config, "config is not found")
      config!!
    }
  }
}
