package com.intellij.lang.jsgraphql.validation;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import org.jetbrains.annotations.NotNull;

public class GraphQLSchemaValidationTest extends GraphQLTestCaseBase {
    @Override
    protected @NotNull String getBasePath() {
        return "/validation/schema";
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        enableAllInspections();
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

    public void testImplementingArgumentsErrors() {
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

    public void testSchemaOperationTypeErrors() {
        doHighlightingTest();
    }

    public void testDeprecatedMembers() {
        doHighlightingTest();
    }

}
