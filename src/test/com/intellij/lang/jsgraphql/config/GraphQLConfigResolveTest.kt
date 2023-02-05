package com.intellij.lang.jsgraphql.config

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.config.GraphQLConfigProvider
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLConfig
import com.intellij.lang.jsgraphql.ide.config.model.GraphQLProjectConfig
import junit.framework.TestCase

class GraphQLConfigResolveTest : GraphQLTestCaseBase() {

    override fun getBasePath(): String = "/config/resolve"

    fun testSkipEmptyFiles() {
        val config = doTestResolveProjectConfig("some/nested/dir/nested.graphql")
        TestCase.assertEquals("graphql.config.yml", config.file?.name)
    }

    private fun doTestResolveConfig(filePath: String): GraphQLConfig {
        myFixture.copyDirectoryToProject(getTestName(true), "/")
        reloadConfiguration()
        val context = myFixture.configureFromTempProjectFile(filePath)
        TestCase.assertNotNull(context)
        val config = GraphQLConfigProvider.getInstance(project).resolveConfig(context)
        TestCase.assertNotNull(config)
        return config!!
    }

    fun doTestResolveProjectConfig(filePath: String): GraphQLProjectConfig {
        val config = doTestResolveConfig(filePath)
        val psiFile = myFixture.file
        TestCase.assertNotNull(psiFile)
        val projectConfig = config.matchProject(psiFile!!)
        TestCase.assertNotNull(projectConfig)
        return projectConfig!!
    }
}
