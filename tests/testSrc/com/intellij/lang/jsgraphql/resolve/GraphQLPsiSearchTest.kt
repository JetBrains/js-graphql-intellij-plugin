package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.openapi.application.smartReadAction
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors

class GraphQLPsiSearchTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/resolve/search"

  fun testFragments() = runBlockingCancellable {
    initTestProject()
    val file = myFixture.configureFromTempProjectFile("fragments-in-js.js")
    val actual = smartReadAction(project) {
      val fragments = GraphQLPsiSearchHelper.getInstance(project).findFragmentDefinitions(file)
      fragments.map { it.name }
    }
    assertSameElements(actual, listOf("SomeFragmentInJs", "SomeFragmentInTs", "FragmentInVue"))
  }

  fun testNamedElements() = runBlockingCancellable {
    initTestProject()
    val expectedNames = listOf("User", "userId", "userName", "UserInput", "newUserId", "newUserName", "UserRole", "ADMIN", "USER")

    val actual = smartReadAction(project) {
      val scope = GlobalSearchScope.projectScope(project)
      val processor = CommonProcessors.CollectProcessor<PsiNamedElement>()
      for (expectedName in expectedNames) {
        GraphQLPsiSearchHelper.getInstance(project).processNamedElements(project, expectedName, scope, processor)
      }
      processor.results.map { it.name }
    }

    assertSameElements(actual, expectedNames)
  }

  fun testInjections() = runBlockingCancellable {
    initTestProject()
    val actual = smartReadAction(project) {
      val scope = GlobalSearchScope.projectScope(project)
      val processor = CommonProcessors.CollectProcessor<GraphQLFile>()
      GraphQLPsiSearchHelper.getInstance(project).processInjectedGraphQLFiles(project, scope, processor)
      processor.results.map { it.name }
    }
    assertSameElements(actual, listOf("injections-in-js.js", "injections-in-ts.ts", "injections-in-vue.vue"))
  }
}