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
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.junit.Test;

import java.util.List;


/**
 * Verifies that two schemas can be separated using graphql-config
 */
public class JSGraphQLSchemaGraphQLConfigCodeInsightTest extends LightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ApplicationManager.getApplication().saveSettings();
        myFixture.configureByFiles("schema-one/.graphqlconfig");
        myFixture.configureByFiles("schema-one/schema-one.graphql");
        myFixture.configureByFiles("schema-two/.graphqlconfig");
        myFixture.configureByFiles("schema-two/schema-two.graphql");
        myFixture.configureByFiles("schema-two/schema-excluded-two.graphql");
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/graphql-config";
    }

    // ---- completion ----

    @Test
    public void testCompletionSchemaOne() {
        doTestCompletion("schema-one/query-one.graphql", Lists.newArrayList("fieldOne"));
    }

    @Test
    public void testCompletionSchemaTwo() {
        doTestCompletion("schema-two/query-two.graphql", Lists.newArrayList("fieldTwo"));
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
