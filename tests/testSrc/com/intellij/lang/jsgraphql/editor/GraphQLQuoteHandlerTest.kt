package com.intellij.lang.jsgraphql.editor

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLQuoteHandlerTest {
  private val graphQLFixture by graphQLTestFixture()

  @Test
  fun testSingleQuote() {
    graphQLFixture.fixture.configureByText("schema.graphql", "<caret> type Abc {}")
    graphQLFixture.fixture.type('"')
    graphQLFixture.fixture.checkResult("\"<caret>\" type Abc {}")
  }

  @Test
  fun testSingleQuoteBackspace() {
    graphQLFixture.fixture.configureByText("schema.graphql", "\"<caret>\" type Abc {}")
    graphQLFixture.fixture.type('\b')
    graphQLFixture.fixture.checkResult("<caret> type Abc {}")
  }
}
