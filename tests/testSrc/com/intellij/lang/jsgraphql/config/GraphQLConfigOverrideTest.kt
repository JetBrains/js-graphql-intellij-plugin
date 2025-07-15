/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.*
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.workspaceModel.ide.impl.WorkspaceEntityLifecycleSupporterUtils
import junit.framework.TestCase

class GraphQLConfigOverrideTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/config/override"

  override fun setUp() {
    super.setUp()

    enableAllInspections()
  }

  fun testOverrideComment() = runBlockingCancellable {
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
      assertEquals(it.second, it.first, parseOverrideConfigComment(it.second))
    }
  }

  fun testScratch() = runBlockingCancellable {
    doScratchTest(
      "/src/child/.graphqlrc.yml",
      GraphQLConfig.DEFAULT_PROJECT,
      createOverrideConfigComment("/src/child/.graphqlrc.yml", null),
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

  fun testScratchWithProject() = runBlockingCancellable {
    doScratchTest(
      "/src/.graphqlrc.yml",
      "frontend",
      createOverrideConfigComment("/src/.graphqlrc.yml", "frontend"),
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

  fun testScratchFallbackToRoot() = runBlockingCancellable {
    doScratchTest(
      "/src/.graphqlrc.yml",
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

  fun testScratchFallbackToRootIfUnknownPathProvided() = runBlockingCancellable {
    doScratchTest(
      "/src/.graphqlrc.yml",
      GraphQLConfig.DEFAULT_PROJECT,
      createOverrideConfigComment("/src/does/not/exist/.graphqlrc.yml", null),
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
    WorkspaceEntityLifecycleSupporterUtils.ensureAllEntitiesInWorkspaceAreAsProvidersDefined(project)
    initTestProject()
    val scratchFile = createTestScratchFile(myFixture, comment, query)!!
    myFixture.configureFromExistingVirtualFile(scratchFile)

    val projectConfig = resolveConfig(scratchFile)
    TestCase.assertEquals(expectedConfigPath, projectConfig.file?.path)
    TestCase.assertEquals(expectedProject, projectConfig.name)

    myFixture.checkHighlighting()
  }

  private suspend fun resolveConfig(file: VirtualFile): GraphQLProjectConfig {
    return smartReadAction(project) {
      val config = GraphQLConfigProvider.getInstance(project).resolveProjectConfig(file)
      assertNotNull("config is not found", config)
      config!!
    }
  }
}
