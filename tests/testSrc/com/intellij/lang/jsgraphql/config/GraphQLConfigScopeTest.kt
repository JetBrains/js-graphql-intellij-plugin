package com.intellij.lang.jsgraphql.config

import com.intellij.idea.IJIgnore
import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigContributor
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.introspection.source.GraphQLGeneratedSourcesManager
import com.intellij.lang.jsgraphql.reloadGraphQLConfiguration
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestDataPath($$"$PROJECT_ROOT/plugins/js-graphql/tests/testData/graphql/config/scope")
@TestApplication
class GraphQLConfigScopeTest {
  private val graphQLFixture by graphQLTestFixture("/config/scope", initTestProject = true)

  @Test
  fun testRootProject(): Unit = timeoutRunBlocking {
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

  @Test
  fun testRecursiveGlobIncludingCurrent(): Unit = timeoutRunBlocking {
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

  @Test
  fun testRecursiveGlobFromNestedDir(): Unit = timeoutRunBlocking {
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

  @Test
  fun testJsonSchema(): Unit = timeoutRunBlocking {
    // The name of the generated SDL file is a hash of the source path, and the temp directory of the project changes in each run.
    val sourceFile = graphQLFixture.fixture.findFileInTempDir("dir/remoteSchema.json")
    val generatedFile = GraphQLGeneratedSourcesManager.getInstance(graphQLFixture.project).requestGeneratedFile(sourceFile)
    assertNotNull(generatedFile, "the SDL file is not generated")
    val expectedSchemas = setOf(
      "dir/remoteSchema.json",
      generatedFile!!.path,
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest(".graphqlrc.yml", expectedSchemas, expectedDocuments)
  }

  @Test
  fun testExcludeNested(): Unit = timeoutRunBlocking {
    val expectedSchemas = setOf(
      "some/some.graphql",
      "some/nested/nested.graphql",
      "some/nested/deep/deep.graphql",
      "some/nested/deep/src/src.graphql",
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest("graphql.config.yml", expectedSchemas, expectedDocuments)
  }

  @Test
  fun testAbsolutePath(): Unit = timeoutRunBlocking {
    // The schema path in the config is absolute, so the test puts the path of the temp directory into the config.
    val configFile = graphQLFixture.fixture.findFileInTempDir("graphql.config.yml")!!
    val tempDirPath = graphQLFixture.tempDir.path
    edtWriteAction {
      VfsUtil.saveText(configFile, VfsUtilCore.loadText(configFile).replace($$"$TEMP_DIR$", tempDirPath))
    }
    graphQLFixture.fixture.reloadGraphQLConfiguration()

    val expectedSchemas = setOf(
      "some/dir/schema.graphql",
    )

    val expectedDocuments = emptySet<String>()

    doScopeTest("graphql.config.yml", expectedSchemas, expectedDocuments)
  }

  @Test
  fun testOverriddenScope(): Unit = timeoutRunBlocking {
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
          GraphQLConfig(project, graphQLFixture.fixture.findFileInTempDir("main/graphql/servicea/"), null, GraphQLRawConfig.EMPTY),
          GraphQLConfig(project, graphQLFixture.fixture.findFileInTempDir("main/graphql/serviceb/"), null, customConfig),
        )
      }
    }, graphQLFixture.fixture.testRootDisposable)
    graphQLFixture.fixture.reloadGraphQLConfiguration()

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

  @Test
  fun testShared(): Unit = timeoutRunBlocking {
    doScopeTest("graphql.config.yml", setOf("lib/common.graphql"), emptySet(), "lib")
    doScopeTest("graphql.config.yml", setOf("lib/common.graphql", "one/one.graphql"), emptySet(), "one")
    doScopeTest("graphql.config.yml", setOf("lib/common.graphql", "two/two.graphql"), emptySet(), "two")
  }

  @Test
  fun testRelativePath(): Unit = timeoutRunBlocking {
    doScopeTest("frontend1/graphql.config.yml", setOf("backend/schema.graphql"), setOf("frontend1/query1.graphql"))
    doScopeTest("frontend2/graphql.config.yml", setOf("backend/schema.graphql"), setOf("frontend2/query2.graphql"))
  }

  @IJIgnore(issue = "WEB-74030")
  @Test
  fun testSchemaInNodeModules(): Unit = timeoutRunBlocking {
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
    val actualFiles = smartReadAction(graphQLFixture.project) { getAllFiles(scope) }
    val expectedFiles = expected.mapTo(mutableSetOf()) {
      val expandedPath = PathMacroManager.getInstance(graphQLFixture.project).expandPathNonNull(it)
      val file = if (FileUtil.isAbsolute(expandedPath)) StandardFileSystems.local().findFileByPath(expandedPath)
      else graphQLFixture.fixture.findFileInTempDir(expandedPath)
      assertNotNull(file, "expected file not found: $it")
      file!!
    }.sortedBy { it.name }.toSet()
    assertThat(actualFiles).describedAs(message).hasSameElementsAs(expectedFiles)
  }

  private fun loadConfig(configPath: String): GraphQLConfig {
    val file = graphQLFixture.fixture.findFileInTempDir(configPath)!!
    val config = GraphQLConfigProvider.getInstance(graphQLFixture.project).getForConfigFile(file)
    assertNotNull(config)
    return config!!
  }

  private fun getAllFiles(scope: GlobalSearchScope): Set<VirtualFile> {
    IndexingTestUtil.waitUntilIndexesAreReady(graphQLFixture.project)
    val result = mutableSetOf<VirtualFile>()
    // need to include files outside of content root, which are processed by ProjectFileIndex
    result.addAll(FileTypeIndex.getFiles(GraphQLFileType.INSTANCE, scope))

    ProjectFileIndex.getInstance(graphQLFixture.project).iterateContent({
                                                           result.add(it)
                                                           true
                                                         }) {
      scope.contains(it)
    }

    val libraryManager = GraphQLLibraryManager.getInstance(graphQLFixture.project)
    return result
      .filterNot { libraryManager.isLibraryRoot(it) }
      .sortedBy { it.path }
      .toSet()
  }
}
