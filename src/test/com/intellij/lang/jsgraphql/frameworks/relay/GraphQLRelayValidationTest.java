package com.intellij.lang.jsgraphql.frameworks.relay;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import org.jetbrains.annotations.NotNull;

public class GraphQLRelayValidationTest extends GraphQLBaseTestCase {
    @Override
    protected @NotNull String getBasePath() {
        return "/frameworks/relay/validation";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GraphQLSettings.getSettings(getProject()).setEnableRelayModernFrameworkSupport(true);
    }

    public void testSuppressedInspections() {
        doHighlightingTest();
    }
}
