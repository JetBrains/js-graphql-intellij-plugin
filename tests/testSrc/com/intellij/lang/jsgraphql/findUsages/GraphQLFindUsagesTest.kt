package com.intellij.lang.jsgraphql.findUsages

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.psi.PsiNamedElement

class GraphQLFindUsagesTest : GraphQLTestCaseBase() {

  override fun getBasePath(): String = "/findUsages"

  override fun setUp() {
    super.setUp()
    runBlockingCancellable {
      initTestProject()
    }
  }

  fun testFragments() = runBlockingCancellable {
    myFixture.configureFromTempProjectFile("${getTestName(true)}.graphql")
    val usageInfos = smartReadAction(project) { myFixture.findUsages(myFixture.elementAtCaret) }

    assertSize(3, usageInfos)
    val containingDeclarationNames = readAction {
      usageInfos
        .map { GraphQLResolveUtil.findContainingDefinition(it.element) }
        .filterIsInstance<PsiNamedElement>()
        .map { it.name }
    }
    assertSameElements(containingDeclarationNames, "Frag2", "Frag3", "Query1")
  }
}
