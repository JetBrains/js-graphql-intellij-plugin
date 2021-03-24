package com.intellij.lang.jsgraphql.validation;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import org.jetbrains.annotations.NotNull;

public class GraphQLSchemaValidationTest extends GraphQLBaseTestCase {
    @Override
    protected @NotNull String getBasePath() {
        return "/validation";
    }

    public void testTypeRegistryRedefinitionErrors() {
        doTest();
    }

//    public void testNotInputOutputTypesErrors() {
//        doTest();
//    }

//    public void testSchemaValidatorErrors() {
//        doTest();
//    }

    private void doTest() {
        myFixture.configureByFile(getTestName(false) + ".graphql");
        myFixture.checkHighlighting();
    }
}
