/**
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.jsgraphql.GraphQLBaseTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class GraphQLOperationsCompletionTest extends GraphQLBaseTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        myFixture.configureByFiles("CompletionSchema.graphqls");
    }

    @Override
    protected @NotNull String getBasePath() {
        return "/completion/operations";
    }

    // ---- completion ----

    public void testCompletionOperation() {
        doTestCompletion("CompletionOperation.graphql", Lists.newArrayList("directive", "enum", "extend", "fragment", "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union", "{"));
    }

    // -- root field names --

    public void testCompletionRootField1() {
        doTestCompletion("CompletionRootField1.graphql", Lists.newArrayList("human", "node", "nodes", "user", "users"));
    }

    public void testCompletionRootField2() {
        doTestCompletion("CompletionRootField2.graphql", Lists.newArrayList("human", "node", "nodes", "user", "users"));
    }

    public void testCompletionRootField3() {
        doTestCompletion("CompletionRootField3.graphql", Lists.newArrayList("human", "node", "nodes", "user", "users"));
    }

    // -- field arguments --

    public void testCompletionRootFieldArg1() {
        doTestCompletion("CompletionRootFieldArg1.graphql", Lists.newArrayList("id"));
    }

    public void testCompletionNestedFieldArg1() {
        doTestCompletion("CompletionNestedFieldArg1.graphql", Lists.newArrayList("another", "height"));
    }

    public void testCompletionNestedFieldArg2() {
        doTestCompletion("CompletionNestedFieldArg2.graphql", Lists.newArrayList("Imperial", "Metric"));
    }

    public void testCompletionNestedFieldArg3() {
        doTestCompletion("CompletionNestedFieldArg3.graphql", Lists.newArrayList("another"));
    }

    // --- extend types --

    public void testCompletionFieldExtension() {
        doTestCompletion("CompletionFieldExtension.graphql", Lists.newArrayList("enumField", "extended", "fieldWithArg", "fieldWithEnumArg", "fieldWithEnumInListArg", "fieldWithInput", "id", "name", "search", "...", "__typename"));
    }

    public void testCompletionDuplicatedTypeField() {
        doTestCompletion("CompletionDuplicatedTypeField.graphql", Lists.newArrayList("address", "age", "name", "...", "__typename"));
    }

    // -- fragments --

    public void testCompletionFragmentDefinition() {
        doTestCompletion("CompletionFragmentDefinition.graphql", Lists.newArrayList("Human", "Mutation", "Node", "Query", "SearchResult", "Ship", "User"));
    }

    public void testCompletionFragmentField() {
        doTestCompletion("CompletionFragmentField.graphql", Lists.newArrayList("id", "...", "__typename"));
    }

    public void testCompletionFragmentInlineType() {
        doTestCompletion("CompletionFragmentInlineType.graphql", Lists.newArrayList("Human", "Node", "Ship"));
    }

    public void testCompletionFragmentInlineUnionType() {
        doTestCompletion("CompletionFragmentInlineUnionType.graphql", Lists.newArrayList("Human", "Node", "Ship"));
    }

    public void testCompletionFragmentInlineReference() {
        doTestCompletion("CompletionFragmentInlineReference.graphql", Lists.newArrayList("MyHumanFragment", " on"));
    }

    // -- input objects --

    public void testCompletionInputNestedField1() {
        doTestCompletion("CompletionInputNestedField1.graphql", Lists.newArrayList("inputField1", "nestedField"));
    }

    public void testCompletionInputNestedField2() {
        doTestCompletion("CompletionInputNestedField2.graphql", Lists.newArrayList("val"));
    }

    // -- enums --

    public void testCompletionEnumArgument() {
        doTestCompletion("CompletionEnumArgument.graphql", Lists.newArrayList("Value1", "Value2"));
    }

    public void testCompletionEnumInListArgument() {
        doTestCompletion("CompletionEnumInListArgument.graphql", Lists.newArrayList("Value1", "Value2"));
    }

    // -- variables --

    public void testCompletionVariableUse() {
        doTestCompletion("CompletionVariableUse.graphql", Lists.newArrayList("$second", "$variable"));
    }

    public void testCompletionVariableUseInList() {
        doTestCompletion("CompletionVariableUseInList.graphql", Lists.newArrayList("$second", "$variable"));
    }

    public void testCompletionVariableUseInList2() {
        doTestCompletion("CompletionVariableUseInList2.graphql", Lists.newArrayList("$second", "$variable"));
    }

    public void testCompletionVariablesWithoutPrefix() {
        doTestCompletion(Lists.newArrayList("$var1", "$var3", "$var5"));
    }

    public void testCompletionVariablesListWithoutPrefix() {
        doTestCompletion(Lists.newArrayList("$var5", "$var6"));
    }

    // -- directives --

    public void testCompletionDirectiveOnField() {
        doTestCompletion("CompletionDirectiveOnField.graphql", Lists.newArrayList("include", "onField", "skip"));
    }

    public void testCompletionDirectiveOnFieldArg() {
        doTestCompletion("CompletionDirectiveOnFieldArg.graphql", Lists.newArrayList("if"));
    }


    public void testSubscriptionOperationDirectives() {
        doTestCompletion(Lists.newArrayList("SubscriptionDir", "SubscriptionDir1"));
    }

    private void doTestCompletion(List<String> expectedCompletions) {
        doTestCompletion(getTestName(false) + ".graphql", expectedCompletions);
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        myFixture.configureByFiles(sourceFile);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings(); // NOTE!: will be null of only one matching completion
        assertEquals("Wrong completions", expectedCompletions, completions);
    }

}
