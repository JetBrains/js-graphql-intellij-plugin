package com.intellij.lang.jsgraphql.validation;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import org.jetbrains.annotations.NotNull;

public class GraphQLOperationsValidationTest extends GraphQLTestCaseBase {
    @Override
    protected @NotNull String getBasePath() {
        return "/validation/operations";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        enableAllInspections();
    }

    public void testRepeatableDirectives() {
        doHighlightingTest();
    }

    public void testWrongTypes() {
        doHighlightingTest();
    }

    public void testDuplicates() {
        doHighlightingTest();
    }

}
