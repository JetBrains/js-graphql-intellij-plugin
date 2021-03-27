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

    public void testNotInputOutputTypesErrors() {
        doTest();
    }

    public void testSchemaValidatorErrors() {
        doTest();
    }

    public void testRedefinitionErrors() {
        doTest();
    }

    public void testImplementingErrors() {
        doTest();
    }

    public void testEnumErrors() {
        doTest();
    }

    public void testDirectiveErrors() {
        doTest();
    }

    public void testSchemaDirectiveErrors() {
        doTest();
    }

    public void testObjectSchemaTypeExtensionsErrors() {
        doTest();
    }

    public void testInterfaceSchemaTypeExtensionsErrors() {
        doTest();
    }

    public void testUnionSchemaTypeExtensionsErrors() {
        doTest();
    }

    public void testEnumSchemaTypeExtensionsErrors() {
        doTest();
    }

    public void testScalarSchemaTypeExtensionsErrors() {
        doTest();
    }

    public void testInputSchemaTypeExtensionsErrors() {
        doTest();
    }

    private void doTest() {
        myFixture.configureByFile(getTestName(false) + ".graphql");
        myFixture.checkHighlighting();
    }
}
