package com.intellij.lang.jsgraphql.frameworks.relay;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.lang.jsgraphql.GraphQLSettings;
import com.intellij.lang.jsgraphql.schema.library.GraphQLLibraryManager;
import org.jetbrains.annotations.NotNull;

public class GraphQLRelayValidationTest extends GraphQLTestCaseBase {
    @Override
    protected @NotNull String getBasePath() {
        return "/frameworks/relay/validation";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        GraphQLSettings.getSettings(getProject()).setRelaySupportEnabled(true);
        GraphQLLibraryManager.getInstance(getProject()).notifyLibrariesChanged();
    }

    public void testSuppressedInspections() {
        doHighlightingTest();
    }
}
