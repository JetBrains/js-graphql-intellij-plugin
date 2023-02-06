package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope
import junit.framework.TestCase

class GraphQLConfigScopeTest : GraphQLTestCaseBase() {

    override fun getBasePath(): String = "/config/scope"

    fun testRootProject() {
        val expectedSchemas = setOf(
            "schema.graphql",
            "dir1/nested/other.graphql",
            "dir1/nested/other1.graphql",
            "any/nested/dir/file1.js",
            "any/nested/dir/file2.ts",
            "patterns/query1.graphql",
            "patterns/query3.graphql",
        )

        val expectedDocuments = setOf(
            "included.graphql",
            "included.js",
            "docs/document.graphql",
            "docs/dir/document123.graphql",
            "docs/dir/document345.graphql",
        )

        doScopeTest("graphql.config.yml", expectedSchemas, expectedDocuments)
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
        compareFiles(projectConfig.schemaScope, expectedSchemas)
        compareFiles(projectConfig.scope, expectedSchemas + expectedDocuments)
    }

    private fun compareFiles(
        scope: GlobalSearchScope,
        expected: Set<String>
    ) {
        val actualFiles = getAllFiles(scope)
        val expectedFiles = expected.mapTo(mutableSetOf()) {
            val file = myFixture.findFileInTempDir(it)
            TestCase.assertNotNull("expected file not found: $it", file)
            file
        }.sortedBy { it.name }.toSet()
        assertSameElements(actualFiles, expectedFiles)
    }

    private fun copyProject() {
        myFixture.copyDirectoryToProject(getTestName(true), "/")
        reloadConfiguration()
    }

    private fun loadConfig(configPath: String): GraphQLConfig {
        copyProject()
        val context = myFixture.configureFromTempProjectFile(configPath)!!
        val config = GraphQLConfigProvider.getInstance(project).getConfig(context.virtualFile)
        TestCase.assertNotNull(config)
        return config!!
    }

    private fun getAllFiles(scope: GlobalSearchScope): Set<VirtualFile> {
        val result = mutableListOf<VirtualFile>()
        ProjectFileIndex.getInstance(project).iterateContent({
            result.add(it)
        }) {
            scope.contains(it)
        }
        return result.sortedBy { it.path }.toSet()
    }
}
