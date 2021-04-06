package com.intellij.lang.jsgraphql.validation;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import org.jetbrains.annotations.NotNull;

public class GraphQLSuppressTest extends GraphQLBaseTestCase {
    @Override
    protected @NotNull String getBasePath() {
        return "/validation/suppress";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        enableAllInspections();
    }

    public void testSuppressOnDefinition() {
        doHighlightingTest();
    }

    public void testSuppressOnFile() {
        doHighlightingTest();
    }

    public void testSuppressOnStatementInjected() {
        doHighlightingTest("js");
    }

    public void testSuppressOnFileInjected() {
        doHighlightingTest("js");
    }
}
