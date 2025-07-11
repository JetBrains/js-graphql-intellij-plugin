/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.getTestDataPath
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.reloadProjectConfiguration
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir

class GraphQLModuleLibrariesScopeTest : JavaCodeInsightFixtureTestCase() {

  override fun getTestDataPath(): String = getTestDataPath("/config/scope")

  fun testResolveInJar() {
    val jarName = "graphql-library-api.jar"
    val lib = directoryContent {
      zip(jarName) {
        dir("api-schema") {
          file("graphql-library-api-schema.graphql", """
            type Query {
              user: User
            }
            
            type User {
              name: String
              age: Int
            }  
          """.trimIndent())
        }
      }
    }.generateInVirtualTempDir()

    myFixture.copyDirectoryToProject(getTestName(true), "")
    val module1 = PsiTestUtil.addModule(project, JavaModuleType.getModuleType(), "mod1", myFixture.findFileInTempDir("mod1"))
    val module2 = PsiTestUtil.addModule(project, JavaModuleType.getModuleType(), "mod2", myFixture.findFileInTempDir("mod2"))
    ModuleRootModificationUtil.addDependency(module1, module2)

    val jar = lib.findChild(jarName)!!
    ModuleRootModificationUtil.addModuleLibrary(
      module2, "graphql-library-api", listOf(VfsUtil.getUrlForLibraryRoot(jar.toNioPath())), emptyList())
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    reloadProjectConfiguration(project)

    val configFile = myFixture.findFileInTempDir("mod1/graphql.config.yml")!!
    val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(configFile)?.getDefault()!!

    val schemaFiles = FileTypeIndex.getFiles(GraphQLFileType.INSTANCE, config.schemaScope)
    val expectedFiles = listOf(
      myFixture.findFileInTempDir("mod1/resources/schema.graphql")!!,
      JarFileSystem.getInstance().getJarRootForLocalFile(jar)?.findFile("api-schema/graphql-library-api-schema.graphql")!!
    )
    assertSameElements(schemaFiles, expectedFiles)
  }
}