package com.intellij.lang.jsgraphql.validation;

import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import org.jetbrains.annotations.NotNull;

public class GraphQLSchemaValidationTest extends GraphQLBaseTestCase {
    @Override
    protected @NotNull String getBasePath() {
        return "/validation/schema";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        myFixture.enableInspections(ourGeneralInspections);
    }

    public void testTypeRegistryRedefinitionErrors() {
        doHighlightingTest();
    }

    public void testNotInputOutputTypesErrors() {
        doHighlightingTest();
    }

    public void testSchemaValidatorErrors() {
        doHighlightingTest();
    }

    public void testRedefinitionErrors() {
        doHighlightingTest();
    }

    public void testImplementingErrors() {
        doHighlightingTest();
    }

    public void testEnumErrors() {
        doHighlightingTest();
    }

    public void testDirectiveErrors() {
        doHighlightingTest();
    }

    public void testSchemaDirectiveErrors() {
        doHighlightingTest();
    }

    public void testObjectSchemaTypeExtensionsErrors() {
        doHighlightingTest();
    }

    public void testInterfaceSchemaTypeExtensionsErrors() {
        doHighlightingTest();
    }

    public void testUnionSchemaTypeExtensionsErrors() {
        doHighlightingTest();
    }

    public void testEnumSchemaTypeExtensionsErrors() {
        doHighlightingTest();
    }

    public void testScalarSchemaTypeExtensionsErrors() {
        doHighlightingTest();
    }

    public void testInputSchemaTypeExtensionsErrors() {
        doHighlightingTest();
    }

    public void testRepeatableDirectivesErrors() {
        doHighlightingTest();
    }

    public void testExtendSchemaErrors() {
        doHighlightingTest();
    }

    public void testFieldDefinitionDirectiveErrors() {
        doHighlightingTest();
    }

}
