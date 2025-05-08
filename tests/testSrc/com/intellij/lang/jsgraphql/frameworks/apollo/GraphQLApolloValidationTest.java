package com.intellij.lang.jsgraphql.frameworks.apollo;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import org.jetbrains.annotations.NotNull;

public class GraphQLApolloValidationTest extends GraphQLTestCaseBase {
  @Override
  protected @NotNull String getBasePath() {
    return "/frameworks/apollo/validation";
  }

  public void testLocalFields() {
    doHighlightingTest();
  }
}
