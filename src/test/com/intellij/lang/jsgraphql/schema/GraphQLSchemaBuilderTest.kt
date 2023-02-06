package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.schema.GraphQLSchemaProvider.Companion.getInstance
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaPrinter
import java.util.function.UnaryOperator

class GraphQLSchemaBuilderTest : GraphQLTestCaseBase() {

    companion object {
        private fun getOptions(optionsBuilder: UnaryOperator<SchemaPrinter.Options>?): SchemaPrinter.Options {
            val options = SchemaPrinter.Options.defaultOptions().includeDirectiveDefinitions(false)
            return optionsBuilder?.apply(options) ?: options
        }
    }

    override fun getBasePath() = "/schema/builder"

    fun testObjects() {
        doTest()
    }

    fun testInterfaces() {
        doTest()
    }

    fun testUnions() {
        doTest()
    }

    fun testInputObjects() {
        doTest()
    }

    fun testScalars() {
        doTest()
    }

    fun testEnums() {
        doTest()
    }

    fun testDirectives() {
        doTest { it.includeDirectiveDefinitions(true) }
    }

    fun testSchemas() {
        doTest()
    }

    fun testSpecifiedByAndDeprecatedDirectives() {
        doTest()
    }

    private fun doTest(optionsBuilder: UnaryOperator<SchemaPrinter.Options>? = null) {
        myFixture.configureByFile(getTestName(true) + ".graphql")
        val schemaProvider = getInstance(myFixture.project)
        val schema = schemaProvider.getSchemaInfo(myFixture.file).schema
        myFixture.configureByText("schema.graphql", SchemaPrinter(project, getOptions(optionsBuilder)).print(schema))
        myFixture.checkResultByFile("${getTestName(true)}_schema.graphql")
    }
}
