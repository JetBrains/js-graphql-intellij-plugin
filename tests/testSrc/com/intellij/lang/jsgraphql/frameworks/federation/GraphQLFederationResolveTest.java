package com.intellij.lang.jsgraphql.frameworks.federation;

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase;
import com.intellij.lang.jsgraphql.GraphQLTestUtils;
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition;
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeExtensionDefinition;
import com.intellij.lang.jsgraphql.schema.library.GraphQLBundledLibraryTypes;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class GraphQLFederationResolveTest extends GraphQLResolveTestCaseBase {

  @Override
  protected @NotNull String getBasePath() {
    return "/frameworks/federation/resolve";
  }

  public void testQueryExtensionsService() {
    GraphQLTestUtils.withLibrary(getProject(), GraphQLBundledLibraryTypes.FEDERATION, () -> {
      PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "_service");
      assertContainingDefinition(target, GraphQLObjectTypeExtensionDefinition.class, "Query");
    }, getTestRootDisposable());
  }

  public void testQueryExtensionsServiceField() {
    GraphQLTestUtils.withLibrary(getProject(), GraphQLBundledLibraryTypes.FEDERATION, () -> {
      PsiElement target = doResolveAsTextTest(GraphQLFieldDefinition.class, "sdl");
      assertContainingDefinition(target, GraphQLObjectTypeDefinition.class, "_Service");
    }, getTestRootDisposable());
  }

  public void testQueryExtensionsOnlyOnRootLevel() {
    GraphQLTestUtils.withLibrary(getProject(), GraphQLBundledLibraryTypes.FEDERATION, this::doHighlightingTest, getTestRootDisposable());
  }

  public void testNotResolvedIfDisabled() {
    doHighlightingTest();
  }
}
