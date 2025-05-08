package com.intellij.lang.jsgraphql.injection;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.testFramework.HighlightTestInfo;

public class GraphQLJavaScriptInjectionHighlightingTest extends GraphQLTestCaseBase {

  @Override
  protected String getBasePath() {
    return "/injection/highlighting/js";
  }

  public void testArgument() {
    doTest();
  }

  private void doTest() {
    HighlightTestInfo highlightTestInfo = myFixture.testFile(getTestName(true) + ".js");
    highlightTestInfo.checkSymbolNames();
    highlightTestInfo.test();
  }
}
