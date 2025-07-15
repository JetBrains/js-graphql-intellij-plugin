package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.types.schema.idl.SchemaPrinter
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.psi.PsiFile
import java.util.function.UnaryOperator

class GraphQLSchemaBuilderTest : GraphQLTestCaseBase() {

  companion object {
    private fun getOptions(optionsBuilder: UnaryOperator<SchemaPrinter.Options>?): SchemaPrinter.Options {
      val options = SchemaPrinter.Options.defaultOptions().includeDirectiveDefinitions(false)
      return optionsBuilder?.apply(options) ?: options
    }
  }

  override fun getBasePath() = "/schema/builder"

  fun testObjects() = runBlockingCancellable {
    doTest()
  }

  fun testInterfaces() = runBlockingCancellable {
    doTest()
  }

  fun testUnions() = runBlockingCancellable {
    doTest()
  }

  fun testInputObjects() = runBlockingCancellable {
    doTest()
  }

  fun testScalars() = runBlockingCancellable {
    doTest()
  }

  fun testEnums() = runBlockingCancellable {
    doTest()
  }

  fun testDirectives() = runBlockingCancellable {
    doTest { it.includeDirectiveDefinitions(true) }
  }

  fun testSchemas() = runBlockingCancellable {
    doTest()
  }

  fun testSpecifiedByAndDeprecatedDirectives() = runBlockingCancellable {
    doTest()
  }

  fun testSchemaInInjections() = runBlockingCancellable {
    doProjectTest("type1.graphql")
  }

  fun testRecursiveDefaultObjectValues() = runBlockingCancellable {
    doTest()
  }

  private fun doTest(optionsBuilder: UnaryOperator<SchemaPrinter.Options>? = null) {
    myFixture.configureByFile(getTestName(true) + ".graphql")

    val file = myFixture.file
    checkByExpectedSchema(file, optionsBuilder)
  }

  private suspend fun doProjectTest(fileName: String, optionsBuilder: UnaryOperator<SchemaPrinter.Options>? = null) {
    initTestProject()
    val file = myFixture.configureFromTempProjectFile(fileName)!!
    checkByExpectedSchema(file, optionsBuilder)
  }

  private fun checkByExpectedSchema(
    file: PsiFile?,
    optionsBuilder: UnaryOperator<SchemaPrinter.Options>?,
  ) {
    val schemaProvider = GraphQLSchemaProvider.getInstance(myFixture.project)
    val schema = schemaProvider.getSchemaInfo(file).schema
    myFixture.configureByText("result.graphql", SchemaPrinter(project, getOptions(optionsBuilder)).print(schema))
    myFixture.checkResultByFile("${getTestName(true)}_expected.graphql")
  }
}
