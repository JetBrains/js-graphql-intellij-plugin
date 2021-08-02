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


public class GraphQLSchemaCompletionTest extends GraphQLTestCaseBase {

    @Override
    protected @NotNull String getBasePath() {
        return "/completion/schema";
    }

    public void testImplementsFirstInterface() {
        doTestCompletion("KnownInterface1", "KnownInterface2");
    }

    public void testImplementsSecondInterface() {
        doTestCompletion("KnownInterface2");
    }

    public void testTopLevelKeywords() {
        doTestCompletion("directive", "enum", "extend", "fragment", "input", "interface", "mutation", "query", "scalar", "schema", "subscription", "type", "union", "{");
    }

    public void testImplementsKeyword1() {
        doTestCompletion("implements");
    }

    public void testImplementsKeyword2() {
        doTestCompletion("implements");
    }

    public void testFieldType() {
        doTestCompletion("AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType", "MyEnum", "MyUnion", "String");
    }

    public void testInputFieldType() {
        doTestCompletion("Boolean", "Float", "ID", "Int", "MyCompletionInput", "MyEnum", "MyInput1", "String");
    }

    public void testArgumentType() {
        doTestCompletion("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String");
    }

    public void testSecondArgumentType() {
        doTestCompletion("Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String");
    }

    public void testDirective1() {
        doTestCompletion("deprecated", "foo");
    }

    public void testDirective2() {
        doTestCompletion("deprecated", "foo");
    }

    public void testDirective3() {
        // TODO: currently not supported by the completion contributor
        //doTestCompletion("Directive3.graphql", "arg");
    }

    public void testDirective4() {
        // TODO: currently not supported by the completion contributor
        //doTestCompletion("Directive4.graphql", "false", "true");
    }

    public void testDirectiveLocations() {
        doTestCompletion(
            "ARGUMENT_DEFINITION", "ENUM", "ENUM_VALUE", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION",
            "FRAGMENT_SPREAD", "INLINE_FRAGMENT", "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT", "QUERY",
            "SCALAR", "SCHEMA", "SUBSCRIPTION", "UNION", "VARIABLE_DEFINITION"
        );
    }

    public void testDirectiveLocations1() {
        doTestCompletion(
            "ENUM", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION",
            "FRAGMENT_SPREAD", "INLINE_FRAGMENT", "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT",
            "SCALAR", "SCHEMA", "SUBSCRIPTION", "UNION", "VARIABLE_DEFINITION"
        );
    }

    public void testDirectiveKeywords() {
        doTestCompletion("on", "repeatable");
    }

    public void testDirectiveKeywords1() {
        doTestCompletion("on");
    }

    public void testFieldOverride() {
        doTestCompletion("fieldToImpl2: Boolean");
    }

    public void testSchemaDirectives() {
        doTestCompletion("DirSchema", "DirSchemaRepeatable");
    }

    public void testSchemaExtensionDirectives() {
        doTestCompletion("DirSchema", "DirSchemaRepeatable");
    }

    public void testExtendKeywords() {
        doTestCompletion("enum", "input", "interface", "scalar", "schema", "type", "union");
    }

    public void testDefaultArgumentObjectValue() {
        doTestCompletion("ASC", "DESC");
    }

    private void doTestCompletion(String @NotNull ... expectedCompletions) {
        doTestCompletion(getTestName(false) + ".graphql", Arrays.asList(expectedCompletions));
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        myFixture.configureByFile(sourceFile);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings();
        assertEquals("Wrong completions", expectedCompletions, completions);
    }

}
