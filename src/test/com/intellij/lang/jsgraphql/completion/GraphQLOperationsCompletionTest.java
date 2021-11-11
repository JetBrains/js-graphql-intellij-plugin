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


public class GraphQLOperationsCompletionTest extends GraphQLCompletionTestCaseBase {

    @Override
    protected @NotNull String getBasePath() {
        return "/completion/operations";
    }

    // ---- completion ----

    public void testOperation() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "directive", "enum", "extend", "fragment",
            "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union");
    }

    // -- root field names --

    public void testRootField1() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "human", "node", "nodes", "user", "users");
    }

    public void testRootField2() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "human", "node", "nodes", "user", "users");
    }

    public void testRootField3() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "human", "node", "nodes", "user", "users");
    }

    // -- field arguments --

    public void testRootFieldArg1() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "id");
    }

    public void testNestedFieldArg1() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "another", "height");
    }

    public void testNestedFieldArg2() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Imperial", "Metric");
    }

    public void testNestedFieldArg3() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "another");
    }

    // --- extend types --

    public void testFieldExtension() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "enumField", "extended", "fieldWithArg", "fieldWithEnumArg", "fieldWithEnumInListArg",
            "fieldWithInput", "id",
            "name", "search", "...", "__typename");
    }

    public void testDuplicatedTypeField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "address", "age", "name", "...", "__typename");
    }

    // -- fragments --

    public void testFragmentDefinition() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Human", "Mutation", "Node", "Query", "SearchResult", "Ship", "User");
    }

    public void testFragmentField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "id", "...", "__typename");
    }

    public void testFragmentInlineType() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Human", "Node", "Ship");
    }

    public void testFragmentInlineUnionType() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Human", "Node", "Ship");
    }

    public void testFragmentInlineReference() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "MyHumanFragment", " on");
    }

    // -- input objects --

    public void testInputNestedField1() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "inputField1", "nestedField");
    }

    public void testInputNestedField2() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "val");
    }

    // -- enums --

    public void testEnumArgument() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Value1", "Value2");
    }

    public void testEnumInListArgument() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Value1", "Value2");
    }

    // -- variables --

    public void testVariableUse() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$second", "$variable");
    }

    public void testVariableUseInList() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$second", "$variable");
    }

    public void testVariableUseInList2() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$second", "$variable");
    }

    public void testVariablesWithoutPrefix() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$var1", "$var3", "$var5");
    }

    public void testVariablesListWithoutPrefix() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$var5", "$var6");
    }

    // -- directives --

    public void testDirectiveOnField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "include", "onField", "skip");
    }

    public void testDirectiveOnFieldArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "if");
    }

    public void testSubscriptionOperationDirectives() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "SubscriptionDir", "SubscriptionDir1");
    }

}
