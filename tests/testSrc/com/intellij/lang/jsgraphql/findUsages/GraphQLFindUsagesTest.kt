package com.intellij.lang.jsgraphql.findUsages

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.ide.resolve.GraphQLResolveUtil
import com.intellij.openapi.application.readAction
import com.intellij.openapi.application.smartReadAction
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLFindUsagesTest {
  private val graphQLFixture by graphQLTestFixture("/findUsages", initTestProject = true)

  @Test
  fun testFragments(): Unit = timeoutRunBlocking {
    graphQLFixture.fixture.configureFromTempProjectFile("${graphQLFixture.getTestName(true)}.graphql")
    val usageInfos = smartReadAction(graphQLFixture.project) { graphQLFixture.fixture.findUsages(graphQLFixture.fixture.elementAtCaret) }

    assertThat(usageInfos).hasSize(3)
    val containingDeclarationNames = readAction {
      usageInfos
        .map { GraphQLResolveUtil.findContainingDefinition(it.element) }
        .filterIsInstance<PsiNamedElement>()
        .map { it.name }
    }
    assertThat(containingDeclarationNames).containsOnly("Frag2", "Frag3", "Query1")
  }
}
