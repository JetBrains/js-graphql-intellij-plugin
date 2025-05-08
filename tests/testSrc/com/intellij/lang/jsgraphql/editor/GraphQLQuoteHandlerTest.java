package com.intellij.lang.jsgraphql.editor;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;

public class GraphQLQuoteHandlerTest extends GraphQLTestCaseBase {

  public void testSingleQuote() {
    myFixture.configureByText("schema.graphql", "<caret> type Abc {}");
    myFixture.type('"');
    myFixture.checkResult("\"<caret>\" type Abc {}");
  }

  public void testSingleQuoteBackspace() {
    myFixture.configureByText("schema.graphql", "\"<caret>\" type Abc {}");
    myFixture.type('\b');
    myFixture.checkResult("<caret> type Abc {}");
  }
}
