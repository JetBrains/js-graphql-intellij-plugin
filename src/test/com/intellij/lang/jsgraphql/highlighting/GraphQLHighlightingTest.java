package com.intellij.lang.jsgraphql.highlighting;

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import com.intellij.testFramework.HighlightTestInfo;

public class GraphQLHighlightingTest extends GraphQLTestCaseBase {

    @Override
    protected String getBasePath() {
        return "/highlighting";
    }

    public void testOperationDefinitions() {
        doTest();
    }

    public void testFragmentDefinition() {
        doTest();
    }

    public void testFragmentSpread() {
        doTest();
    }

    public void testTypeNameDefinitions() {
        doTest();
    }

    public void testFields() {
        doTest();
    }

    public void testFieldDefinitions() {
        doTest();
    }

    public void testInputFieldDefinitions() {
        doTest();
    }

    public void testArgumentDefinitions() {
        doTest();
    }

    public void testArguments() {
        doTest();
    }

    public void testVariables() {
        doTest();
    }

    public void testValues() {
        doTest();
    }

    public void testDirectives() {
        doTest();
    }

    public void testKeywordsAsIdentifiers() {
        doTest();
    }

    private void doTest() {
        HighlightTestInfo highlightTestInfo = myFixture.testFile(getTestName(false) + ".graphql");
        highlightTestInfo.checkSymbolNames();
        highlightTestInfo.test();
    }
}
