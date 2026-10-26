package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaPrinter
import com.intellij.psi.PsiFile
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test
import java.util.function.UnaryOperator

@TestApplication
class GraphQLSchemaBuilderTest {
  private val graphQLFixture by graphQLTestFixture("/schema/builder")

  companion object {
    private fun getOptions(optionsBuilder: UnaryOperator<SchemaPrinter.Options>?): SchemaPrinter.Options {
      val options = SchemaPrinter.Options.defaultOptions().includeDirectiveDefinitions(false)
      return optionsBuilder?.apply(options) ?: options
    }
  }

  @Test
  fun testObjects() {
    doTest()
  }

  @Test
  fun testInterfaces() {
    doTest()
  }

  @Test
  fun testUnions() {
    doTest()
  }

  @Test
  fun testInputObjects() {
    doTest()
  }

  @Test
  fun testScalars() {
    doTest()
  }

  @Test
  fun testEnums() {
    doTest()
  }

  @Test
  fun testDirectives() {
    doTest { it.includeDirectiveDefinitions(true) }
  }

  @Test
  fun testSchemas() {
    doTest()
  }

  @Test
  fun testSpecifiedByAndDeprecatedDirectives() {
    doTest()
  }

  @Test
  fun testSchemaInInjections(): Unit = timeoutRunBlocking {
    doProjectTest("type1.graphql")
  }

  @Test
  fun testRecursiveDefaultObjectValues() {
    doTest()
  }

  private fun doTest(optionsBuilder: UnaryOperator<SchemaPrinter.Options>? = null) {
    graphQLFixture.fixture.configureByFile(graphQLFixture.getTestName(true) + ".graphql")

    val file = graphQLFixture.fixture.file
    checkByExpectedSchema(file, optionsBuilder)
  }

  private suspend fun doProjectTest(fileName: String, optionsBuilder: UnaryOperator<SchemaPrinter.Options>? = null) {
    graphQLFixture.initTestProject()
    val file = graphQLFixture.fixture.configureFromTempProjectFile(fileName)!!
    checkByExpectedSchema(file, optionsBuilder)
  }

  private fun checkByExpectedSchema(
    file: PsiFile?,
    optionsBuilder: UnaryOperator<SchemaPrinter.Options>?,
  ) {
    val schemaProvider = GraphQLSchemaProvider.getInstance(graphQLFixture.project)
    val schema = schemaProvider.getSchemaInfo(file).schema
    val text = SchemaPrinter(graphQLFixture.project, getOptions(optionsBuilder)).print(schema)
    graphQLFixture.fixture.configureByText("result.graphql", text)
    graphQLFixture.fixture.checkResultByFile("${graphQLFixture.getTestName(true)}_expected.graphql")
  }
}
