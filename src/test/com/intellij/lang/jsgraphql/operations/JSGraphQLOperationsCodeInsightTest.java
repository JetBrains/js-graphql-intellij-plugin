/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.operations;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.junit.Test;

import java.util.List;


public class JSGraphQLOperationsCodeInsightTest extends LightPlatformCodeInsightFixtureTestCase {

    private PsiFile[] psiFiles;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        psiFiles = myFixture.configureByFiles("CompletionSchema.graphqls");
    }

    @Override
    protected void tearDown() throws Exception {
        // clean up the schema discovery caches
        for (PsiFile psiFile : psiFiles) {
            PsiDocumentManager documentManager = PsiDocumentManager.getInstance(psiFile.getProject());
            final Document document = documentManager.getDocument(psiFile);
            assertNotNull(document);
            ApplicationManager.getApplication().runWriteAction(() -> {
                document.setText("");
                documentManager.commitAllDocuments();
            });
        }
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData/graphql/operations";
    }

    // ---- completion ----

    @Test
    public void testCompletionOperation() {
        doTestCompletion("CompletionOperation.graphql", Lists.newArrayList("directive", "enum", "extend", "fragment", "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union", "{"));
    }

    // -- root field names --

    @Test
    public void testCompletionRootField1() {
        doTestCompletion("CompletionRootField1.graphql", Lists.newArrayList("human", "node", "nodes"));
    }

    @Test
    public void testCompletionRootField2() {
        doTestCompletion("CompletionRootField1.graphql", Lists.newArrayList("human", "node", "nodes"));
    }

    @Test
    public void testCompletionRootField3() {
        doTestCompletion("CompletionRootField1.graphql", Lists.newArrayList("human", "node", "nodes"));
    }

    // -- field arguments --

    @Test
    public void testCompletionRootFieldArg1() {
        doTestCompletion("CompletionRootFieldArg1.graphql", Lists.newArrayList("id"));
    }

    @Test
    public void testCompletionNestedFieldArg1() {
        doTestCompletion("CompletionNestedFieldArg1.graphql", Lists.newArrayList("another", "height"));
    }

    @Test
    public void testCompletionNestedFieldArg2() {
        doTestCompletion("CompletionNestedFieldArg2.graphql", Lists.newArrayList("Imperial", "Metric"));
    }

    @Test
    public void testCompletionNestedFieldArg3() {
        doTestCompletion("CompletionNestedFieldArg3.graphql", Lists.newArrayList("another"));
    }

    // --- extend types --

    @Test
    public void testCompletionFieldExtension() {
        doTestCompletion("CompletionFieldExtension.graphql", Lists.newArrayList("enumField", "extended", "fieldWithArg", "fieldWithEnumArg", "fieldWithEnumInListArg", "fieldWithInput", "id", "name", "search", "...", "__typename"));
    }


    // -- fragments --

    @Test
    public void testCompletionFragmentDefinition() {
        doTestCompletion("CompletionFragmentDefinition.graphql", Lists.newArrayList("Human", "Mutation", "Node", "Query", "SearchResult", "Ship"));
    }

    @Test
    public void testCompletionFragmentField() {
        doTestCompletion("CompletionFragmentField.graphql", Lists.newArrayList("id", "...", "__typename"));
    }

    @Test
    public void testCompletionFragmentInlineType() {
        doTestCompletion("CompletionFragmentInlineType.graphql", Lists.newArrayList("Human", "Node", "Ship"));
    }

    @Test
    public void testCompletionFragmentInlineUnionType() {
        doTestCompletion("CompletionFragmentInlineUnionType.graphql", Lists.newArrayList("Human", "Node", "Ship"));
    }

    @Test
    public void testCompletionFragmentInlineReference() {
        doTestCompletion("CompletionFragmentInlineReference.graphql", Lists.newArrayList("MyHumanFragment", " on"));
    }


    // -- input objects --

    @Test
    public void testCompletionInputNestedField1() {
        doTestCompletion("CompletionInputNestedField1.graphql", Lists.newArrayList("inputField1", "nestedField"));
    }

    @Test
    public void testCompletionInputNestedField2() {
        doTestCompletion("CompletionInputNestedField2.graphql", Lists.newArrayList("val"));
    }


    // -- enums --

    @Test
    public void testCompletionEnumArgument() {
        doTestCompletion("CompletionEnumArgument.graphql", Lists.newArrayList("Value1", "Value2"));
    }

    @Test
    public void testCompletionEnumInListArgument() {
        doTestCompletion("CompletionEnumInListArgument.graphql", Lists.newArrayList("Value1", "Value2"));
    }


    // -- variables --

    @Test
    public void testCompletionVariableUse() {
        doTestCompletion("CompletionVariableUse.graphql", Lists.newArrayList("$second", "$variable"));
    }

    @Test
    public void testCompletionVariableUseInList() {
        doTestCompletion("CompletionVariableUseInList.graphql", Lists.newArrayList("$second", "$variable"));
    }

    @Test
    public void testCompletionVariableUseInList2() {
        doTestCompletion("CompletionVariableUseInList2.graphql", Lists.newArrayList("$second", "$variable"));
    }


    // -- directives --

    @Test
    public void testCompletionDirectiveOnField() {
        doTestCompletion("CompletionDirectiveOnField.graphql", Lists.newArrayList("include", "onField", "skip"));
    }

    @Test
    public void testCompletionDirectiveOnFieldArg() {
        doTestCompletion("CompletionDirectiveOnFieldArg.graphql", Lists.newArrayList("if"));
    }



    // ---- util ----
    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        myFixture.configureByFiles(sourceFile);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings(); // NOTE!: will be null of only one matching completion
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
