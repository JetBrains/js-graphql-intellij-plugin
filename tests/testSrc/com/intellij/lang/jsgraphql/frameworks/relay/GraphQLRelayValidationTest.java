package com.intellij.lang.jsgraphql.frameworks.relay;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.GraphQLTestUtils;
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes;
import org.jetbrains.annotations.NotNull;

public class GraphQLRelayValidationTest extends GraphQLTestCaseBase {
  @Override
  protected @NotNull String getBasePath() {
    return "/frameworks/relay/validation";
  }

  public void testSuppressedInspections() {
    GraphQLTestUtils.withLibrary(getProject(), GraphQLBundledLibraryTypes.RELAY, this::doHighlightingTest, getTestRootDisposable());
  }
}
