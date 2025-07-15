package com.intellij.lang.jsgraphql.config

import com.intellij.idea.IJIgnore
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigContributor
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath

@TestDataPath($$"$CONTENT_ROOT/testData/graphql/config/scope")
class GraphQLConfigScopeTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/config/scope"

  override fun setUp() {
    super.setUp()

    runBlockingCancellable {
      initTestProject()
    }
  }

  fun testRootProject() = runBlockingCancellable {
    val expectedSchemas = setOf(
      "schema.graphql",
      "dir1/nested/other.graphql",
      "dir1/nested/other1.graphql",
      "any/nested/dir/file1.js",
      "any/nested/dir/file2.ts",
      "patterns/query1.graphql",
      "patterns/query3.graphql",
      "included.graphql",
      "included.ts"
    )

    val expectedDocuments = setOf(
      "docs/document.graphql",
      "docs/dir/document123.graphql",
      "docs/dir/document345.graphql",
      "notReallyExcludedDocument.graphql",
    )

    doScopeTest("graphql.config.yml", expectedSchemas, expectedDocuments)
  }

  fun testRecursiveGlobIncludingCurrent() = runBlockingCancellable {
    val expectedSchemas = setOf(
      "file.graphql",
      "file1.graphql",
      "dir2/file2.graphql",
      "dir2/dir3/file3.graphql",
      "dir2/dir3/dir4/file4.graphql",
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest(".graphqlrc.yml", expectedSchemas, expectedDocuments)
  }

  fun testRecursiveGlobFromNestedDir() = runBlockingCancellable {
    val expectedSchemas = setOf(
      "dir2/file2.graphql",
      "dir2/dir3/file3.graphql",
      "dir2/dir3/dir4/file4.graphql",
    )

    val expectedDocuments = setOf(
      "file.graphql",
      "file1.graphql",
    )

    doScopeTest(".graphqlrc.yml", expectedSchemas, expectedDocuments)
  }

  fun testJsonSchema() = runBlockingCancellable {
    val expectedSchemas = setOf(
      "dir/remoteSchema.json",
      $$"$APPLICATION_CONFIG_DIR$/graphql/sdl/6833dafe39e404965a21449cbb58bc232e8b364ee5eb08fa1652f29fe081515c.graphql"
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest(".graphqlrc.yml", expectedSchemas, expectedDocuments)
  }

  fun testExcludeNested() = runBlockingCancellable {
    val expectedSchemas = setOf(
      "some/some.graphql",
      "some/nested/nested.graphql",
      "some/nested/deep/deep.graphql",
      "some/nested/deep/src/src.graphql",
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest("graphql.config.yml", expectedSchemas, expectedDocuments)
  }

  fun testAbsolutePath() = runBlockingCancellable {
    val expectedSchemas = setOf(
      "some/dir/schema.graphql",
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest("graphql.config.yml", expectedSchemas, expectedDocuments)
  }

  fun testOverriddenScope() = runBlockingCancellable {
    GraphQLConfigContributor.EP_NAME.point.registerExtension(object : GraphQLConfigContributor {
      override fun contributeConfigs(project: Project): Collection<GraphQLConfig> {
        val customConfig = GraphQLRawConfig(
          schema = listOf(
            GraphQLRawSchemaPointer(pattern = "**/schema.graphql"),
            GraphQLRawSchemaPointer(pattern = "directives.graphql")
          ),
          documents = listOf("operations.graphql"),
          exclude = listOf("ignored.graphql")
        )
        return listOf(
          GraphQLConfig(project, myFixture.findFileInTempDir("main/graphql/servicea/"), null, GraphQLRawConfig.EMPTY),
          GraphQLConfig(project, myFixture.findFileInTempDir("main/graphql/serviceb/"), null, customConfig),
        )
      }
    }, testRootDisposable)
    myFixture.reloadGraphQLConfiguration()

    doScopeTest(
      "main/graphql/servicea",
      setOf(
        "main/graphql/servicea/schema.graphql",
        "main/graphql/servicea/operations.graphql",
      ),
      emptySet(),
    )

    doScopeTest(
      "main/graphql/serviceb",
      setOf(
        "main/graphql/serviceb/nested/dir/schema.graphql",
        "main/graphql/serviceb/directives.graphql",
      ),
      setOf(
        "main/graphql/serviceb/operations.graphql",
      ),
    )
  }

  fun testShared() = runBlockingCancellable {
    doScopeTest("graphql.config.yml", setOf("lib/common.graphql"), emptySet(), "lib")
    doScopeTest("graphql.config.yml", setOf("lib/common.graphql", "one/one.graphql"), emptySet(), "one")
    doScopeTest("graphql.config.yml", setOf("lib/common.graphql", "two/two.graphql"), emptySet(), "two")
  }

  fun testRelativePath() = runBlockingCancellable {
    doScopeTest("frontend1/graphql.config.yml", setOf("backend/schema.graphql"), setOf("frontend1/query1.graphql"))
    doScopeTest("frontend2/graphql.config.yml", setOf("backend/schema.graphql"), setOf("frontend2/query2.graphql"))
  }

  @IJIgnore(issue = "WEB-74030")
  fun testSchemaInNodeModules() = runBlockingCancellable {
    doScopeTest("graphql.config.yml", setOf("node_modules/@octokit/graphql-schema/schema.graphql"), emptySet())
  }

  private suspend fun doScopeTest(
    configPath: String,
    expectedSchemas: Set<String>,
    expectedDocuments: Set<String>,
    projectName: String? = null,
  ) {
    val config = loadConfig(configPath)
    val projectConfig =
      projectName
        ?.let { checkNotNull(config.findProject(projectName)) }
      ?: checkNotNull(config.getDefault())
    compareFiles("strict schema scope is invalid", readAction { projectConfig.schemaScope }, expectedSchemas)
    compareFiles("documents scope is invalid", readAction { projectConfig.scope }, expectedSchemas + expectedDocuments)
  }

  private suspend fun compareFiles(
    message: String,
    scope: GlobalSearchScope,
    expected: Set<String>,
  ) {
    val actualFiles = smartReadAction(project) { getAllFiles(scope) }
    val expectedFiles = expected.mapTo(mutableSetOf()) {
      val expandedPath = PathMacroManager.getInstance(project).expandPath(it)
      val file = myFixture.findFileInTempDir(expandedPath)
                 ?: LocalFileSystem.getInstance().findFileByPath(expandedPath)
      assertNotNull("expected file not found: $it", file)
      file!!
    }.sortedBy { it.name }.toSet()
    assertSameElements(message, actualFiles, expectedFiles)
  }

  private fun loadConfig(configPath: String): GraphQLConfig {
    val file = myFixture.findFileInTempDir(configPath)!!
    val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(file)
    assertNotNull(config)
    return config!!
  }

  private fun getAllFiles(scope: GlobalSearchScope): Set<VirtualFile> {
    IndexingTestUtil.waitUntilIndexesAreReady(project)
    val result = mutableSetOf<VirtualFile>()
    // need to include files outside of content root, which are processed by ProjectFileIndex
    result.addAll(FileTypeIndex.getFiles(GraphQLFileType.INSTANCE, scope))

    ProjectFileIndex.getInstance(project).iterateContent({
                                                           result.add(it)
                                                           true
                                                         }) {
      scope.contains(it)
    }

    val libraryManager = GraphQLLibraryManager.getInstance(project)
    return result
      .filterNot { libraryManager.isLibraryRoot(it) }
      .sortedBy { it.path }
      .toSet()
  }
}
