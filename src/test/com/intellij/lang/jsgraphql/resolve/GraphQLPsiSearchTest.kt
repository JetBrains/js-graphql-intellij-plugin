package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors

class GraphQLPsiSearchTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/resolve/search"

  fun testFragments() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    val file = myFixture.configureFromTempProjectFile("fragments-in-js.js")
    val fragments = GraphQLPsiSearchHelper.getInstance(project).findFragmentDefinitions(file)
    assertSameElements(fragments.map { it.name }, listOf("SomeFragmentInJs", "SomeFragmentInTs", "FragmentInVue"))
  }

  fun testNamedElements() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    val scope = GlobalSearchScope.projectScope(project)
    val processor = CommonProcessors.CollectProcessor<PsiNamedElement>()
    val expectedNames = listOf("User", "userId", "userName", "UserInput", "newUserId", "newUserName", "UserRole", "ADMIN", "USER")
    for (expectedName in expectedNames) {
      GraphQLPsiSearchHelper.getInstance(project).processNamedElements(project, expectedName, scope, processor)
    }
    assertSameElements(processor.results.map { it.name }, expectedNames)
  }

  fun testInjections() {
    myFixture.copyDirectoryToProject(getTestName(true), "")
    val scope = GlobalSearchScope.projectScope(project)
    val processor = CommonProcessors.CollectProcessor<GraphQLFile>()
    GraphQLPsiSearchHelper.getInstance(project).processInjectedGraphQLFiles(project, scope, processor)
    assertSameElements(processor.results.map { it.name }, listOf("injections-in-js.js", "injections-in-ts.ts", "injections-in-vue.vue"))
  }
}