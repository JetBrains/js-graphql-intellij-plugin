/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion

import com.intellij.lang.jsgraphql.GraphQLCompletionTestCaseBase
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLOperationsCompletionTest : GraphQLCompletionTestCaseBase() {
  override fun getBasePath(): String {
    return "/completion/operations"
  }

  // -- root field names --
  fun testFieldRootQuerySelectionSet() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename")
    checkResult(lookupElements, "user")
  }

  fun testFieldRootNamedQuery() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename")
    checkResult(lookupElements, "__typename")
  }

  fun testFieldRootNamedQueryAnonymous() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename")
    checkResult(lookupElements, "human")
  }

  fun testFieldTypeExtension() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "enumField", "extended", "fieldWithArg", "fieldWithEnumArg", "fieldWithEnumInListArg",
                       "fieldWithInput", "id", "name", "search", "__typename")
    checkResult(lookupElements, "fieldWithEnumArg")
  }

  fun testFieldDuplicatedFields() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "address", "age", "name", "__typename")
  }

  fun testFieldDeprecated() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkContainsAll(lookupElements, "deprecatedField")
    checkDeprecated(lookupElements, "deprecatedField", true)
  }

  fun testFieldTypeText() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkTypeText(lookupElements, "name", "String!")
    checkTypeText(lookupElements, "search", "[SearchResult]")
    checkTypeText(lookupElements, "extended", "Boolean")
  }

  fun testFieldRootMutation() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements,
                       "createIssue", "createIssues", "createMember", "createMembers",
                       "createOrganization", "createOrganizations", "__typename"
    )
    checkResult(lookupElements, "createIssue")
  }

  fun testFieldRootSubscription() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "issues", "members", "organizations", "__typename")
    checkResult(lookupElements, "issues")
  }

  fun testFieldBracesInsertionObject() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "user")
  }

  fun testFieldBracesInsertionObjectWithArguments() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "userWithArgs")
  }

  fun testFieldBracesInsertionObjectWithRequiredArguments() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "userWithRequiredArgs")
  }

  fun testFieldBracesInsertionObjectNonNull() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "userNonNull")
  }

  fun testFieldBracesInsertionUnionList() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "unionList")
  }

  fun testFieldBracesInsertionInterface() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "interface")
  }

  fun testFieldBracesInsertionScalar() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "scalar")
  }

  fun testFieldBracesInsertionDoNotAddIfExists() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "user")
  }

  fun testFieldBracesInsertionDoNotAddIfExistsOnNewLine() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(BRACES_INSERTION_SCHEMA)
    checkResult(lookupElements, "user")
  }

  // -- field arguments --
  fun testArgumentNameRootQueryField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "filter", "name", "zipCodes")
    checkResult(lookupElements, "name")
  }

  fun testArgumentNameRootQueryExtensionField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "from", "to")
    checkResult(lookupElements, "from")
  }

  fun testArgumentNameNestedQueryField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "another", "height")
    checkResult(lookupElements, "height")
  }

  fun testArgumentValueNestedQueryField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "Imperial", "Metric")
    checkResult(lookupElements, "Metric")
  }

  fun testArgumentNameNestedQueryFieldSecond() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "another")
    checkResult(lookupElements, "another")
  }

  fun testArgumentBooleanValue() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "false", "true")
    checkResult(lookupElements, "false")
  }

  // -- fragments --
  fun testFragmentDefinitionTypeCondition() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements,
                       "Assignable", "Bug", "Deletable", "Described", "EmailOwner", "Feature", "Issue", "IssueListResponse", "ListResponse",
                       "Milestone", "NamedNode", "Node", "NodeListResponse", "Organization", "OrganizationListResponse", "Release",
                       "Repository", "RepositoryListResponse", "RootMutation", "RootQuery", "RootSubscription",
                       "Task", "TeamMember", "TeamMemberListResponse", "Timestamped")
    checkResult(lookupElements, "Organization")
  }

  fun testFragmentDefinitionNestedInlineFragmentTypeConditionObject() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "NamedNode", "Node", "TeamMember", "Timestamped")

    // TODO: [vepanimas] ignores interfaces from extensions
    // checkEqualsOrdered(lookupElements, "EmailOwner", "NamedNode", "Node", "TeamMember", "Timestamped");
  }

  fun testFragmentDefinitionNestedInlineFragmentTypeConditionUnion() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "Assignable", "Bug", "Described", "Feature", "Node", "Timestamped")

    // TODO: [vepanimas] ignores enum extensions
    // checkEqualsOrdered(lookupElements, "Assignable", "Bug", "Described", "Feature", "Node", "Task", "Timestamped");
  }

  fun testFragmentDefinitionNestedInlineFragmentTypeConditionInterface() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "Deletable", "Repository")

    // TODO: [vepanimas] ignores interface extensions
    // checkEqualsOrdered(lookupElements, "Deletable", "Repository", "Timestamped");
  }

  fun testFragmentInlineTypeConditionObjectType() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "NamedNode", "Node", "TeamMember", "Timestamped")
  }

  fun testFragmentInlineReference() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "Fragment1", "Fragment2", "Fragment3", "Fragment4", "on")
    checkResult(lookupElements, "Fragment4")
  }

  fun testFragmentInlineReferenceUnionType() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "Fragment1", "Fragment2", "Fragment3", "Fragment4", "on")
  }

  fun testFragmentField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "createdAt", "email", "id", "name", "phone",
                       "role", "status", "updatedAt", "__typename")
  }

  fun testFragmentInlineField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements, "createdAt", "email", "id", "name", "phone",
                       "role", "status", "updatedAt", "__typename")
  }

  fun testFragmentInlineOnComplexInterface() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    checkEqualsOrdered(lookupElements,
                       "Bug", "Feature", "Milestone", "Node", "Organization", "Release", "Task", "TeamMember")

    // TODO: [vepanimas] these types are expected here, should traverse all reachable types in hierarchy
    // checkEqualsOrdered(lookupElements,
    //     "Assignable", "Bug", "Described", "EmailOwner", "Feature", "Milestone", "NamedNode", "Node",
    //     "Organization", "Release", "Task", "TeamMember", "Timestamped");
  }

  fun testFragmentIncompleteNoFields() = runBlockingCancellable {
    val lookupElements = doTestWithSchema(OTHER_SCHEMA)
    assertNotNull(lookupElements)
    assertEmpty(lookupElements!!)
  }

  fun testFragmentInlineWithoutTypeCondition() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "address", "age", "name", "__typename")
  }

  // -- input objects --
  fun testInputNestedField1() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "inputField1", "nestedField")
  }

  fun testInputNestedField2() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "val")
  }

  // -- enums --
  fun testEnumArgument() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "Value1", "Value2")
  }

  fun testEnumInListArgument() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "Value1", "Value2")
    checkResult(lookupElements, "Value2")
  }

  // -- variables --
  fun testVariableNonNullTypeForNonNullArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$id")
  }

  fun testVariableNonNullTypeForNullableArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$name")
  }

  fun testVariableNullableTypeForNonNullArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEmpty(lookupElements)
  }

  fun testVariableEnumType() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$second", "\$variable")
    checkTypeText(lookupElements, "\$second", "MyEnum!")
    checkTypeText(lookupElements, "\$variable", "MyEnum")
  }

  fun testVariableListTypeForNonNullArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEmpty(lookupElements)
  }

  fun testVariableListTypeForNullableListNullableArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$four", "\$one", "\$three", "\$two")
  }

  fun testVariableInListTypeForNullableListNullableArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$enum", "\$enum1", "\$four", "\$one", "\$three", "\$two")

    // TODO: [vepanimas] should match only inner types
    // checkEqualsOrdered(lookupElements, "$enum", "$enum1");
  }

  fun testVariableListTypeForNonNullListNullableArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$four", "\$two")
  }

  fun testVariableInListTypeForNonNullListNullableArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$four", "\$string1", "\$two")

    // TODO: [vepanimas] should match only inner types
    // checkEqualsOrdered(lookupElements, "$string1");
  }

  fun testVariableListTypeForNullableListNonNullArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$four", "\$three")
  }

  fun testVariableInListTypeForNullableListNonNullArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$four", "\$id", "\$id1", "\$three")

    // TODO: [vepanimas] should match only inner types
    // checkEqualsOrdered(lookupElements, "$id", "$id1");
  }

  fun testVariableListTypeForNonNullListNonNullArg() = runBlockingCancellable {
    doTestWithSchema()
    checkResult()
  }

  fun testVariableInListTypeForNonNullListNonNullArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$four", "\$id1")

    // TODO: [vepanimas] should match only inner types - only `$id1` in that case
    // doTestWithSchema();
    // checkResult();
  }

  fun testVariableUseInList() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$second", "\$variable")
  }

  fun testVariableWithoutPrefix() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$var1", "\$var3", "\$var5")
  }

  fun testVariableListTypeWithoutPrefix() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "\$var5", "\$var6")
  }

  fun testVariableDefinitionType() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements,
                       "Boolean", "Float", "ID", "Int", "MyEnum", "MyInput",
                       "MyNestedInput", "OtherObject", "String", "Units", "UserFilter")
    checkResult(lookupElements, "UserFilter")
  }

  // -- directives --
  fun testDirectiveOnField() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "include", "onField", "skip")
  }

  fun testDirectiveOnFieldArg() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "if")
  }

  fun testSubscriptionOperationDirectives() = runBlockingCancellable {
    val lookupElements = doTestWithSchema()
    checkEqualsOrdered(lookupElements, "SubscriptionDir", "SubscriptionDir1")
  }

  fun testEnumArgumentInInjection() = runBlockingCancellable {
    val lookupElements = doTestWithProject(".js")
    checkEqualsOrdered(lookupElements, "ADMIN", "GUEST", "USER")
  }

  fun testInputFieldAsVariableDefaultValue() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "criterion")
    checkResult(lookupElements, "criterion")
  }

  companion object {
    private const val OTHER_SCHEMA = "SchemaOther.graphql"
    private const val BRACES_INSERTION_SCHEMA = "SchemaBracesInsertion.graphql"
  }
}
