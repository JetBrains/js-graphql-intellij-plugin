package com.intellij.lang.jsgraphql.introspection

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.editor.LogicalPosition
import junit.framework.TestCase

class GraphQLIntrospectionLineMarkersTest : GraphQLTestCaseBase() {
    override fun getBasePath() = "/introspection/lineMarkers"

    fun testYaml() {
        doTest("graphql.config.yml", listOf(8, 9))
    }

    fun testYamlProjects() {
        doTest("graphql.config.yml", listOf(23, 31, 48, 49))
    }

    private fun doTest(name: String, expectedGutterOffsets: List<Int>) {
        myFixture.configureByFile("${getTestName(true)}/$name")
        reloadConfiguration()
        val allGutters = myFixture.findAllGutters()
        TestCase.assertEquals("total gutters count", expectedGutterOffsets.size, allGutters.size)
        expectedGutterOffsets.forEach {
            myFixture.editor.caretModel.moveToLogicalPosition(LogicalPosition(it, 0))
            val gutters = myFixture.findGuttersAtCaret()
            TestCase.assertEquals("invalid gutters count at line $it", 1, gutters.size)
            TestCase.assertEquals(
                "invalid gutter tooltip at line $it",
                GraphQLBundle.message("graphql.introspection.run.query"),
                gutters[0].tooltipText,
            )
        }
    }
}
