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
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.util.List;


public class GraphQLCompletionTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/completion";
    }

    public void testCompletionImplementsFirstInterface() {
        doTestCompletion("CompletionImplementsFirstInterface.graphqls", Lists.newArrayList("KnownInterface1", "KnownInterface2"));
    }

    public void testCompletionImplementsSecondInterface() {
        doTestCompletion("CompletionImplementsSecondInterface.graphqls", Lists.newArrayList("KnownInterface2"));
    }

    public void testCompletionTopLevelKeywords() {
        doTestCompletion("CompletionTopLevelKeywords.graphqls", Lists.newArrayList("directive", "enum", "extend", "fragment", "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union", "{"));
    }

    public void testCompletionImplementsKeyword1() {
        doTestCompletion("CompletionImplementsKeyword1.graphqls", Lists.newArrayList("implements"));
    }

    public void testCompletionImplementsKeyword2() {
        doTestCompletion("CompletionImplementsKeyword2.graphqls", Lists.newArrayList("implements"));
    }

    public void testCompletionFieldType() {
        doTestCompletion("CompletionFieldType.graphqls", Lists.newArrayList("AnotherKnownType", "Boolean", "Float", "ID", "Int", "KnownInterface", "KnownType", "MyEnum", "MyUnion", "String"));
    }

    public void testCompletionInputFieldType() {
        doTestCompletion("CompletionInputFieldType.graphqls", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyEnum", "MyInput1", "String"));
    }

    public void testCompletionArgumentType() {
        doTestCompletion("CompletionArgumentType.graphqls", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
    }

    public void testCompletionSecondArgumentType() {
        doTestCompletion("CompletionSecondArgumentType.graphqls", Lists.newArrayList("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String"));
    }

    public void testCompletionDirective1() {
        doTestCompletion("CompletionDirective1.graphqls", Lists.newArrayList("deprecated", "foo"));
    }

    public void testCompletionDirective2() {
        doTestCompletion("CompletionDirective2.graphqls", Lists.newArrayList("deprecated", "foo"));
    }

    public void testCompletionDirective3() {
        // TODO: currently not supported by the completion contributor
        //doTestCompletion("CompletionDirective3.graphqls", Lists.newArrayList("arg"));
    }

    public void testCompletionDirective4() {
        // TODO: currently not supported by the completion contributor
        //doTestCompletion("CompletionDirective4.graphqls", Lists.newArrayList("false", "true"));
    }

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

}
