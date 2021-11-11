/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.jsgraphql.GraphQLCompletionTestCaseBase;
import org.jetbrains.annotations.NotNull;


public class GraphQLKeywordsCompletionTest extends GraphQLCompletionTestCaseBase {

    @Override
    protected @NotNull String getBasePath() {
        return "/completion/keywords";
    }

    public void testTopLevelKeywords() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
            "input", "interface", "mutation", "query", "scalar",
            "schema", "subscription", "type", "union");
        checkResult(lookupElements, "input");
    }

    public void testTopLevelKeywordsOnlyAtLineStart() {
        LookupElement[] lookupElements = doTest();
        checkDoesNotContain(lookupElements, "directive", "enum", "extend", "fragment",
            "input", "interface", "mutation", "query", "scalar",
            "schema", "subscription", "type", "union");
    }

    public void testTopLevelKeywordsAfterComment() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
            "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union");
    }

    public void testTopLevelBrace() {
        // initially was provided via keyword completion
        doTest();
        myFixture.type('{');
        myFixture.checkResultByFile(getTestName(false) + "_after.graphql");
    }

    public void testExtendKeywords() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "enum", "input", "interface", "scalar", "schema", "type", "union");
    }

    public void testDirectiveKeywords() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "on", "repeatable");
        checkResult(lookupElements, "repeatable");
    }

    public void testDirectiveKeywords1() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "on");
    }

    public void testImplementsKeyword1() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "implements");
        checkResult(lookupElements, "implements");
    }

    public void testImplementsKeyword2() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "implements");
        checkResult(lookupElements, "implements");
    }

    public void testImplementsKeywordExtendType() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "implements");
    }

    public void testImplementsKeywordInterface() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "implements");
    }

    public void testImplementsKeywordExtendInterface() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "implements");
    }

    public void testSchemaOperationNames() {
        LookupElement[] lookupElements = doTest();
        checkEqualsOrdered(lookupElements, "mutation", "subscription");
        checkResult(lookupElements, "mutation");
    }
}
