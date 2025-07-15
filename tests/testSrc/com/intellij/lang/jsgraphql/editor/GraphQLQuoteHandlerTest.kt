package com.intellij.lang.jsgraphql.editor

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLQuoteHandlerTest : GraphQLTestCaseBase() {
  fun testSingleQuote() = runBlockingCancellable {
    myFixture.configureByText("schema.graphql", "<caret> type Abc {}")
    myFixture.type('"')
    myFixture.checkResult("\"<caret>\" type Abc {}")
  }

  fun testSingleQuoteBackspace() = runBlockingCancellable {
    myFixture.configureByText("schema.graphql", "\"<caret>\" type Abc {}")
    myFixture.type('\b')
    myFixture.checkResult("<caret> type Abc {}")
  }
}
