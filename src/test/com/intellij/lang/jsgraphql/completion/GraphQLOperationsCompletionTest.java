/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.jsgraphql.GraphQLTestCaseBase;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;


public class GraphQLOperationsCompletionTest extends GraphQLTestCaseBase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.configureByFile("Schema.graphql");
    }

    @Override
    protected @NotNull String getBasePath() {
        return "/completion/operations";
    }

    // ---- completion ----

    public void testOperation() {
        doTestCompletion("directive", "enum", "extend", "fragment", "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union", "{");
    }

    // -- root field names --

    public void testRootField1() {
        doTestCompletion("human", "node", "nodes", "user", "users");
    }

    public void testRootField2() {
        doTestCompletion("human", "node", "nodes", "user", "users");
    }

    public void testRootField3() {
        doTestCompletion("human", "node", "nodes", "user", "users");
    }

    // -- field arguments --

    public void testRootFieldArg1() {
        doTestCompletion("id");
    }

    public void testNestedFieldArg1() {
        doTestCompletion("another", "height");
    }

    public void testNestedFieldArg2() {
        doTestCompletion("Imperial", "Metric");
    }

    public void testNestedFieldArg3() {
        doTestCompletion("another");
    }

    // --- extend types --

    public void testFieldExtension() {
        doTestCompletion("enumField", "extended", "fieldWithArg", "fieldWithEnumArg", "fieldWithEnumInListArg", "fieldWithInput", "id", "name", "search", "...", "__typename");
    }

    public void testDuplicatedTypeField() {
        doTestCompletion("address", "age", "name", "...", "__typename");
    }

    // -- fragments --

    public void testFragmentDefinition() {
        doTestCompletion("Human", "Mutation", "Node", "Query", "SearchResult", "Ship", "User");
    }

    public void testFragmentField() {
        doTestCompletion("id", "...", "__typename");
    }

    public void testFragmentInlineType() {
        doTestCompletion("Human", "Node", "Ship");
    }

    public void testFragmentInlineUnionType() {
        doTestCompletion("Human", "Node", "Ship");
    }

    public void testFragmentInlineReference() {
        doTestCompletion("MyHumanFragment", " on");
    }

    // -- input objects --

    public void testInputNestedField1() {
        doTestCompletion("inputField1", "nestedField");
    }

    public void testInputNestedField2() {
        doTestCompletion("val");
    }

    // -- enums --

    public void testEnumArgument() {
        doTestCompletion("Value1", "Value2");
    }

    public void testEnumInListArgument() {
        doTestCompletion("Value1", "Value2");
    }

    // -- variables --

    public void testVariableUse() {
        doTestCompletion("$second", "$variable");
    }

    public void testVariableUseInList() {
        doTestCompletion("$second", "$variable");
    }

    public void testVariableUseInList2() {
        doTestCompletion("$second", "$variable");
    }

    public void testVariablesWithoutPrefix() {
        doTestCompletion("$var1", "$var3", "$var5");
    }

    public void testVariablesListWithoutPrefix() {
        doTestCompletion("$var5", "$var6");
    }

    // -- directives --

    public void testDirectiveOnField() {
        doTestCompletion("include", "onField", "skip");
    }

    public void testDirectiveOnFieldArg() {
        doTestCompletion("if");
    }

    public void testSubscriptionOperationDirectives() {
        doTestCompletion("SubscriptionDir", "SubscriptionDir1");
    }

    private void doTestCompletion(String @NotNull ... expectedCompletions) {
        doTestCompletion(getTestName(false) + ".graphql", Arrays.asList(expectedCompletions));
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        myFixture.configureByFile(sourceFile);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings(); // NOTE!: will be null of only one matching completion
        assertEquals("Wrong completions", expectedCompletions, completions);
    }

}
