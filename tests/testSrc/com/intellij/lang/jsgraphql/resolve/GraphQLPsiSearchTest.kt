package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.search.GraphQLPsiSearchHelper
import com.intellij.lang.jsgraphql.psi.GraphQLFile
import com.intellij.openapi.application.smartReadAction
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.util.CommonProcessors
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLPsiSearchTest {
  private val graphQLFixture by graphQLTestFixture("/resolve/search")

  @Test
  fun testFragments(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val file = graphQLFixture.fixture.configureFromTempProjectFile("fragments-in-js.js")
    val actual = smartReadAction(graphQLFixture.project) {
      val fragments = GraphQLPsiSearchHelper.getInstance(graphQLFixture.project).findFragmentDefinitions(file)
      fragments.map { it.name }
    }
    assertThat(actual).hasSameElementsAs(listOf("SomeFragmentInJs", "SomeFragmentInTs", "FragmentInVue"))
  }

  @Test
  fun testNamedElements(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val expectedNames = listOf("User", "userId", "userName", "UserInput", "newUserId", "newUserName", "UserRole", "ADMIN", "USER")

    val actual = smartReadAction(graphQLFixture.project) {
      val scope = GlobalSearchScope.projectScope(graphQLFixture.project)
      val processor = CommonProcessors.CollectProcessor<PsiNamedElement>()
      for (expectedName in expectedNames) {
        GraphQLPsiSearchHelper.getInstance(graphQLFixture.project)
          .processNamedElements(graphQLFixture.project, expectedName, scope, processor)
      }
      processor.results.map { it.name }
    }

    assertThat(actual).hasSameElementsAs(expectedNames)
  }

  @Test
  fun testInjections(): Unit = timeoutRunBlocking {
    graphQLFixture.initTestProject()
    val actual = smartReadAction(graphQLFixture.project) {
      val scope = GlobalSearchScope.projectScope(graphQLFixture.project)
      val processor = CommonProcessors.CollectProcessor<GraphQLFile>()
      GraphQLPsiSearchHelper.getInstance(graphQLFixture.project).processInjectedGraphQLFiles(graphQLFixture.project, scope, processor)
      processor.results.map { it.name }
    }
    assertThat(actual).hasSameElementsAs(listOf("injections-in-js.js", "injections-in-ts.ts", "injections-in-vue.vue"))
  }
}