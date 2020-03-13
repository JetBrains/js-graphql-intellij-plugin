/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Test;

import java.util.List;


public class JSGraphQLSchemaCodeInsightTest extends LightPlatformCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/schema";
    }

    // ---- completion ----

    @Test
    public void testCompletionImplementsFirstInterface() {
        doTestCompletion("CompletionImplementsFirstInterface.graphqls", Lists.newArrayList("KnownInterface1", "KnownInterface2"));
    }

    @Test
    public void testCompletionImplementsSecondInterface() {
        doTestCompletion("CompletionImplementsSecondInterface.graphqls", Lists.newArrayList("KnownInterface2"));
    }

    @Test
    public void testCompletionTopLevelKeywords() {
        doTestCompletion("CompletionTopLevelKeywords.graphqls", Lists.newArrayList("directive", "enum", "extend", "fragment", "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union", "{"));
    }

    @Test
    public void testCompletionImplementsKeyword1() {
        doTestCompletion("CompletionImplementsKeyword1.graphqls", Lists.newArrayList("implements"));
    }

    @Test
    public void testCompletionImplementsKeyword2() {
        doTestCompletion("CompletionImplementsKeyword2.graphqls", Lists.newArrayList("implements"));
    }

    @Test
    public void testCompletionFieldType() {
        doTestCompletion("CompletionFieldType.graphqls", Lists.newArrayList("AnotherKnownType", "Boolean", "Float", "ID", "Int", "KnownInterface", "KnownType", "MyEnum", "MyUnion", "String"));
    }

    @Test
    public void testCompletionInputFieldType() {
        doTestCompletion("CompletionInputFieldType.graphqls", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyEnum", "MyInput1", "String"));
    }

    @Test
    public void testCompletionArgumentType() {
        doTestCompletion("CompletionArgumentType.graphqls", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
    }

    @Test
    public void testCompletionSecondArgumentType() {
        doTestCompletion("CompletionSecondArgumentType.graphqls", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
    }

    @Test
    public void testCompletionDirective1() {
        doTestCompletion("CompletionDirective1.graphqls", Lists.newArrayList("deprecated", "foo"));
    }

    @Test
    public void testCompletionDirective2() {
        doTestCompletion("CompletionDirective2.graphqls", Lists.newArrayList("deprecated", "foo"));
    }

    @Test
    public void testCompletionDirective3() {
        // TODO: currently not supported by the completion contributor
        //doTestCompletion("CompletionDirective3.graphqls", Lists.newArrayList("arg"));
    }

    @Test
    public void testCompletionDirective4() {
        // TODO: currently not supported by the completion contributor
        //doTestCompletion("CompletionDirective4.graphqls", Lists.newArrayList("false", "true"));
    }

    @Test
    public void testCompletionFieldOverride() {
        doTestCompletion("CompletionFieldOverride.graphqls", Lists.newArrayList("fieldToImpl2: Boolean"));
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        myFixture.configureByFiles(sourceFile);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings();
        assertEquals("Wrong completions", expectedCompletions, completions);
        ApplicationManager.getApplication().runWriteAction(() -> {
            myFixture.getEditor().getDocument().setText(""); // blank out the file so it doesn't affect other tests
            PsiDocumentManager.getInstance(myFixture.getProject()).commitAllDocuments();
        });
    }


    // ---- highlighting -----

    // TODO
//    @Test
//    public void testErrorAnnotator() {
//        myFixture.configureByFiles("ErrorAnnotator.graphqls");
//        myFixture.checkHighlighting(false, false, false);
//    }

}
