package com.intellij.lang.jsgraphql.findUsages

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.UsefulTestCase

class GraphQLFindUsagesTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/findUsages"

  override fun setUp() {
    super.setUp()
    copyProject()
  }

  fun testFragments() {
    myFixture.configureFromTempProjectFile("${getTestName(true)}.graphql")
    val usageInfos = myFixture.findUsages(myFixture.elementAtCaret)

    UsefulTestCase.assertSize(3, usageInfos)
    val containingDeclarationNames = usageInfos
      .map { GraphQLResolveUtil.findContainingDefinition(it.element) }
      .filterIsInstance<PsiNamedElement>()
      .map { it.name }
    assertSameElements(containingDeclarationNames, "Frag2", "Frag3", "Query1")
  }
}
