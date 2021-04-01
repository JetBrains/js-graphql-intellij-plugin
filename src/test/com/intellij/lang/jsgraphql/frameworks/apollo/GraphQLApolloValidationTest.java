package com.intellij.lang.jsgraphql.frameworks.apollo;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import org.jetbrains.annotations.NotNull;

public class GraphQLApolloValidationTest extends GraphQLBaseTestCase {
    @Override
    protected @NotNull String getBasePath() {
        return "/frameworks/apollo/validation";
    }

    public void testLocalFields() {
        doHighlightingTest();
    }
}
