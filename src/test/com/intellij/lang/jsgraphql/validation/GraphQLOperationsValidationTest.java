package com.intellij.lang.jsgraphql.validation;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import org.jetbrains.annotations.NotNull;

public class GraphQLOperationsValidationTest extends GraphQLBaseTestCase {
    @Override
    protected @NotNull String getBasePath() {
        return "/validation/operations";
    }

    public void testRepeatableDirectives() {
        doTest();
    }

    public void testWrongTypes() {
        doTest();
    }

    public void testDuplicates() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile(getTestName(false) + ".graphql");
        myFixture.checkHighlighting();
    }
}
