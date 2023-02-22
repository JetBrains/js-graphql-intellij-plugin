package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLFileType
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigContributor
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawConfig
import com.intellij.lang.jsgraphql.ide.config.loader.GraphQLRawSchemaPointer
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager
import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import junit.framework.TestCase

class GraphQLConfigScopeTest : GraphQLTestCaseBase() {

    override fun getBasePath(): String = "/config/scope"

    override fun setUp() {
        super.setUp()

        copyProject()
    }

    fun testRootProject() {
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

    fun testRecursiveGlobIncludingCurrent() {
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

    fun testRecursiveGlobFromNestedDir() {
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

    fun testJsonSchema() {
        val expectedSchemas = setOf(
            "dir/remoteSchema.json",
            "\$APPLICATION_CONFIG_DIR$/graphql/sdl/6833dafe39e404965a21449cbb58bc232e8b364ee5eb08fa1652f29fe081515c.graphql"
        )

        val expectedDocuments = emptySet<String>()

        doScopeTest(".graphqlrc.yml", expectedSchemas, expectedDocuments)
    }

    fun testOverriddenScope() {
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
        reloadConfiguration()

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

    private fun doScopeTest(
        configPath: String,
        expectedSchemas: Set<String>,
        expectedDocuments: Set<String>,
        projectName: String? = null
    ) {
        val config = loadConfig(configPath)
        val projectConfig =
            projectName
                ?.let { checkNotNull(config.findProject(projectName)) }
                ?: checkNotNull(config.getDefault())
        compareFiles("strict schema scope is invalid", projectConfig.schemaScope, expectedSchemas)
        compareFiles("documents scope is invalid", projectConfig.scope, expectedSchemas + expectedDocuments)
    }

    private fun compareFiles(
        message: String,
        scope: GlobalSearchScope,
        expected: Set<String>
    ) {
        val actualFiles = getAllFiles(scope)
        val expectedFiles = expected.mapTo(mutableSetOf()) {
            val expandedPath = PathMacroManager.getInstance(project).expandPath(it)
            val file = myFixture.findFileInTempDir(expandedPath)
                ?: LocalFileSystem.getInstance().findFileByPath(expandedPath)
            TestCase.assertNotNull("expected file not found: $it", file)
            file!!
        }.sortedBy { it.name }.toSet()
        assertSameElements(message, actualFiles, expectedFiles)
    }

    private fun copyProject() {
        myFixture.copyDirectoryToProject(getTestName(true), "")
        reloadConfiguration()
    }

    private fun loadConfig(configPath: String): GraphQLConfig {
        val file = myFixture.findFileInTempDir(configPath)!!
        val config = GraphQLConfigProvider.getInstance(project).getForConfigFile(file)
        TestCase.assertNotNull(config)
        return config!!
    }

    private fun getAllFiles(scope: GlobalSearchScope): Set<VirtualFile> {
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
