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

    private static final String OTHER_SCHEMA = "OtherSchema.graphql";

    @Override
    protected @NotNull String getBasePath() {
        return "/completion/operations";
    }

    // -- root field names --

    public void testFieldRootQuerySelectionSet() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename");
        checkResult(lookupElements, "user");
    }

    public void testFieldRootNamedQuery() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename");
        checkResult(lookupElements, "__typename");
    }

    public void testFieldRootNamedQueryAnonymous() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename");
        checkResult(lookupElements, "human");
    }

    public void testFieldTypeExtension() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "enumField", "extended", "fieldWithArg", "fieldWithEnumArg", "fieldWithEnumInListArg",
            "fieldWithInput", "id", "name", "search", "__typename");
        checkResult(lookupElements, "fieldWithEnumArg");
    }

    public void testFieldDuplicatedFields() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "address", "age", "name", "__typename");
    }

    public void testFieldDeprecated() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkContainsAll(lookupElements, "deprecatedField");
        checkDeprecated(lookupElements, "deprecatedField", true);
    }

    public void testFieldTypeText() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkTypeText(lookupElements, "name", "String!");
        checkTypeText(lookupElements, "search", "[SearchResult]");
        checkTypeText(lookupElements, "extended", "Boolean");
    }

    public void testFieldRootMutation() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements,
            "createIssue", "createIssues", "createMember", "createMembers",
            "createOrganization", "createOrganizations", "__typename"
        );
        checkResult(lookupElements, "createIssue");
    }

    public void testFieldRootSubscription() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "issues", "members", "organizations", "__typename");
        checkResult(lookupElements, "issues");
    }

    // -- field arguments --

    public void testArgumentNameRootQueryField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "filter", "name", "zipCodes");
        checkResult(lookupElements, "name");
    }

    public void testArgumentNameRootQueryExtensionField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "from", "to");
        checkResult(lookupElements, "from");
    }

    public void testArgumentNameNestedQueryField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "another", "height");
        checkResult(lookupElements, "height");
    }

    public void testArgumentValueNestedQueryField() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "Imperial", "Metric");
        checkResult(lookupElements, "Metric");
    }

    public void testArgumentNameNestedQueryFieldSecond() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "another");
        checkResult(lookupElements, "another");
    }

    public void testArgumentBooleanValue() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "false", "true");
        checkResult(lookupElements, "false");
    }

    // -- fragments --

    public void testFragmentDefinitionTypeCondition() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements,
            "Assignable", "Bug", "Deletable", "Described", "EmailOwner", "Feature", "Issue", "IssueListResponse", "ListResponse",
            "Milestone", "NamedNode", "Node", "NodeListResponse", "Organization", "OrganizationListResponse", "Release",
            "Repository", "RepositoryListResponse", "RootMutation", "RootQuery", "RootSubscription",
            "Task", "TeamMember", "TeamMemberListResponse", "Timestamped");
        checkResult(lookupElements, "Organization");
    }

    public void testFragmentDefinitionNestedInlineFragmentTypeConditionObject() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "NamedNode", "Node", "TeamMember", "Timestamped");

        // TODO: [vepanimas] ignores interfaces from extensions
        // checkEqualsOrdered(lookupElements, "EmailOwner", "NamedNode", "Node", "TeamMember", "Timestamped");
    }

    public void testFragmentDefinitionNestedInlineFragmentTypeConditionUnion() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "Assignable", "Bug", "Described", "Feature", "Node", "Timestamped");

        // TODO: [vepanimas] ignores enum extensions
        // checkEqualsOrdered(lookupElements, "Assignable", "Bug", "Described", "Feature", "Node", "Task", "Timestamped");
    }

    public void testFragmentDefinitionNestedInlineFragmentTypeConditionInterface() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "Deletable", "Repository");

        // TODO: [vepanimas] ignores interface extensions
        // checkEqualsOrdered(lookupElements, "Deletable", "Repository", "Timestamped");
    }

    public void testFragmentInlineTypeConditionObjectType() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "NamedNode", "Node", "TeamMember", "Timestamped");
    }

    public void testFragmentInlineReference() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "Fragment1", "Fragment2", "Fragment3", "Fragment4", "on");
        checkResult(lookupElements, "Fragment4");
    }

    public void testFragmentInlineReferenceUnionType() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "Fragment1", "Fragment2", "Fragment3", "Fragment4", "on");
    }

    public void testFragmentField() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "createdAt", "email", "id", "name", "phone",
            "role", "status", "updatedAt", "__typename");
    }

    public void testFragmentInlineField() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements, "createdAt", "email", "id", "name", "phone",
            "role", "status", "updatedAt", "__typename");
    }

    public void testFragmentInlineOnComplexInterface() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        checkEqualsOrdered(lookupElements,
            "Bug", "Feature", "Milestone", "Node", "Organization", "Release", "Task", "TeamMember");

        // TODO: [vepanimas] these types are expected here, should traverse all reachable types in hierarchy
        // checkEqualsOrdered(lookupElements,
        //     "Assignable", "Bug", "Described", "EmailOwner", "Feature", "Milestone", "NamedNode", "Node",
        //     "Organization", "Release", "Task", "TeamMember", "Timestamped");
    }

    public void testFragmentIncompleteNoFields() {
        LookupElement[] lookupElements = doTestWithSchema(OTHER_SCHEMA);
        assertNotNull(lookupElements);
        assertEmpty(lookupElements);
    }

    public void testFragmentInlineWithoutTypeCondition() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "address", "age", "name", "__typename");
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
        checkResult(lookupElements, "Value2");
    }

    // -- variables --

    public void testVariableNonNullTypeForNonNullArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$id");
    }

    public void testVariableNonNullTypeForNullableArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$name");
    }

    public void testVariableNullableTypeForNonNullArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEmpty(lookupElements);
    }

    public void testVariableEnumType() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$second", "$variable");
        checkTypeText(lookupElements, "$second", "MyEnum!");
        checkTypeText(lookupElements, "$variable", "MyEnum");
    }

    public void testVariableListTypeForNonNullArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEmpty(lookupElements);
    }

    public void testVariableListTypeForNullableListNullableArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$four", "$one", "$three", "$two");
    }

    public void testVariableInListTypeForNullableListNullableArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$enum", "$enum1", "$four", "$one", "$three", "$two");

        // TODO: [vepanimas] should match only inner types
        // checkEqualsOrdered(lookupElements, "$enum", "$enum1");
    }

    public void testVariableListTypeForNonNullListNullableArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$four", "$two");
    }

    public void testVariableInListTypeForNonNullListNullableArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$four", "$string1", "$two");

        // TODO: [vepanimas] should match only inner types
        // checkEqualsOrdered(lookupElements, "$string1");
    }

    public void testVariableListTypeForNullableListNonNullArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$four", "$three");
    }

    public void testVariableInListTypeForNullableListNonNullArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$four", "$id", "$id1", "$three");

        // TODO: [vepanimas] should match only inner types
        // checkEqualsOrdered(lookupElements, "$id", "$id1");
    }

    public void testVariableListTypeForNonNullListNonNullArg() {
        doTestWithSchema();
        checkResult();
    }

    public void testVariableInListTypeForNonNullListNonNullArg() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$four", "$id1");

        // TODO: [vepanimas] should match only inner types - only `$id1` in that case
        // doTestWithSchema();
        // checkResult();
    }

    public void testVariableUseInList() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$second", "$variable");
    }

    public void testVariableWithoutPrefix() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$var1", "$var3", "$var5");
    }

    public void testVariableListTypeWithoutPrefix() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements, "$var5", "$var6");
    }

    public void testVariableDefinitionType() {
        LookupElement[] lookupElements = doTestWithSchema();
        checkEqualsOrdered(lookupElements,
            "Boolean", "Float", "ID", "Int", "MyEnum", "MyInput",
            "MyNestedInput", "OtherObject", "String", "Units", "UserFilter");
        checkResult(lookupElements, "UserFilter");
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
