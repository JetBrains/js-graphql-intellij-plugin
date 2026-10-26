/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.completion

import com.intellij.lang.jsgraphql.graphQLCompletionTestFixture
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLOperationsCompletionTest {
  private val completionFixture by graphQLCompletionTestFixture("/completion/operations")

  // -- root field names --
  @Test
  fun testFieldRootQuerySelectionSet(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename")
    completionFixture.checkResult(lookupElements, "user")
  }

  @Test
  fun testFieldRootNamedQuery(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename")
    completionFixture.checkResult(lookupElements, "__typename")
  }

  @Test
  fun testFieldRootNamedQueryAnonymous(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "human", "ids", "node", "nodes", "user", "users", "__typename")
    completionFixture.checkResult(lookupElements, "human")
  }

  @Test
  fun testFieldTypeExtension(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "enumField", "extended", "fieldWithArg", "fieldWithEnumArg",
                                         "fieldWithEnumInListArg", "fieldWithInput", "id", "name", "search", "__typename")
    completionFixture.checkResult(lookupElements, "fieldWithEnumArg")
  }

  @Test
  fun testFieldDuplicatedFields(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "address", "age", "name", "__typename")
  }

  @Test
  fun testFieldDeprecated(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkContainsAll(lookupElements, "deprecatedField")
    completionFixture.checkDeprecated(lookupElements, "deprecatedField", true)
  }

  @Test
  fun testFieldTypeText(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkTypeText(lookupElements, "name", "String!")
    completionFixture.checkTypeText(lookupElements, "search", "[SearchResult]")
    completionFixture.checkTypeText(lookupElements, "extended", "Boolean")
  }

  @Test
  fun testFieldRootMutation(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements,
                                         "createIssue", "createIssues", "createMember", "createMembers",
                                         "createOrganization", "createOrganizations", "__typename"
    )
    completionFixture.checkResult(lookupElements, "createIssue")
  }

  @Test
  fun testFieldRootSubscription(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "issues", "members", "organizations", "__typename")
    completionFixture.checkResult(lookupElements, "issues")
  }

  @Test
  fun testFieldBracesInsertionObject(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "user")
  }

  @Test
  fun testFieldBracesInsertionObjectWithArguments(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "userWithArgs")
  }

  @Test
  fun testFieldBracesInsertionObjectWithRequiredArguments(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "userWithRequiredArgs")
  }

  @Test
  fun testFieldBracesInsertionObjectNonNull(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "userNonNull")
  }

  @Test
  fun testFieldBracesInsertionUnionList(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "unionList")
  }

  @Test
  fun testFieldBracesInsertionInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "interface")
  }

  @Test
  fun testFieldBracesInsertionScalar(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "scalar")
  }

  @Test
  fun testFieldBracesInsertionDoNotAddIfExists(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "user")
  }

  @Test
  fun testFieldBracesInsertionDoNotAddIfExistsOnNewLine(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(BRACES_INSERTION_SCHEMA)
    completionFixture.checkResult(lookupElements, "user")
  }

  // -- field arguments --
  @Test
  fun testArgumentNameRootQueryField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "filter", "name", "zipCodes")
    completionFixture.checkResult(lookupElements, "name")
  }

  @Test
  fun testArgumentNameRootQueryExtensionField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "from", "to")
    completionFixture.checkResult(lookupElements, "from")
  }

  @Test
  fun testArgumentNameNestedQueryField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "another", "height")
    completionFixture.checkResult(lookupElements, "height")
  }

  @Test
  fun testArgumentValueNestedQueryField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "Imperial", "Metric")
    completionFixture.checkResult(lookupElements, "Metric")
  }

  @Test
  fun testArgumentNameNestedQueryFieldSecond(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "another")
    completionFixture.checkResult(lookupElements, "another")
  }

  @Test
  fun testArgumentBooleanValue(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "false", "true")
    completionFixture.checkResult(lookupElements, "false")
  }

  // -- fragments --
  @Test
  fun testFragmentDefinitionTypeCondition(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements,
                                         "Assignable", "Bug", "Deletable", "Described", "EmailOwner", "Feature", "Issue",
                                         "IssueListResponse", "ListResponse", "Milestone", "NamedNode", "Node", "NodeListResponse",
                                         "Organization", "OrganizationListResponse", "Release", "Repository",
                                         "RepositoryListResponse", "RootMutation", "RootQuery", "RootSubscription",
                                         "Task", "TeamMember", "TeamMemberListResponse", "Timestamped")
    completionFixture.checkResult(lookupElements, "Organization")
  }

  @Test
  fun testFragmentDefinitionNestedInlineFragmentTypeConditionObject(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "NamedNode", "Node", "TeamMember", "Timestamped")

    // TODO: [vepanimas] ignores interfaces from extensions
    // checkEqualsOrdered(lookupElements, "EmailOwner", "NamedNode", "Node", "TeamMember", "Timestamped");
  }

  @Test
  fun testFragmentDefinitionNestedInlineFragmentTypeConditionUnion(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "Assignable", "Bug", "Described", "Feature", "Node", "Timestamped")

    // TODO: [vepanimas] ignores enum extensions
    // checkEqualsOrdered(lookupElements, "Assignable", "Bug", "Described", "Feature", "Node", "Task", "Timestamped");
  }

  @Test
  fun testFragmentDefinitionNestedInlineFragmentTypeConditionInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "Deletable", "Repository")

    // TODO: [vepanimas] ignores interface extensions
    // checkEqualsOrdered(lookupElements, "Deletable", "Repository", "Timestamped");
  }

  @Test
  fun testFragmentInlineTypeConditionObjectType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "NamedNode", "Node", "TeamMember", "Timestamped")
  }

  @Test
  fun testFragmentInlineReference(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "Fragment1", "Fragment2", "Fragment3", "Fragment4", "on")
    completionFixture.checkResult(lookupElements, "Fragment4")
  }

  @Test
  fun testFragmentInlineReferenceUnionType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "Fragment1", "Fragment2", "Fragment3", "Fragment4", "on")
  }

  @Test
  fun testFragmentField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "createdAt", "email", "id", "name", "phone",
                                         "role", "status", "updatedAt", "__typename")
  }

  @Test
  fun testFragmentInlineField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements, "createdAt", "email", "id", "name", "phone",
                                         "role", "status", "updatedAt", "__typename")
  }

  @Test
  fun testFragmentInlineOnComplexInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    completionFixture.checkEqualsOrdered(lookupElements,
                                         "Bug", "Feature", "Milestone", "Node", "Organization", "Release", "Task", "TeamMember")

    // TODO: [vepanimas] these types are expected here, should traverse all reachable types in hierarchy
    // checkEqualsOrdered(lookupElements,
    //     "Assignable", "Bug", "Described", "EmailOwner", "Feature", "Milestone", "NamedNode", "Node",
    //     "Organization", "Release", "Task", "TeamMember", "Timestamped");
  }

  @Test
  fun testFragmentIncompleteNoFields(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema(OTHER_SCHEMA)
    assertNotNull(lookupElements)
    assertThat(lookupElements!!).isEmpty()
  }

  @Test
  fun testFragmentInlineWithoutTypeCondition(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "address", "age", "name", "__typename")
  }

  // -- input objects --
  @Test
  fun testInputNestedField1(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "inputField1", "nestedField")
  }

  @Test
  fun testInputNestedField2(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "val")
  }

  // -- enums --
  @Test
  fun testEnumArgument(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "Value1", "Value2")
  }

  @Test
  fun testEnumInListArgument(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "Value1", "Value2")
    completionFixture.checkResult(lookupElements, "Value2")
  }

  // -- variables --
  @Test
  fun testVariableNonNullTypeForNonNullArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$id")
  }

  @Test
  fun testVariableNonNullTypeForNullableArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$name")
  }

  @Test
  fun testVariableNullableTypeForNonNullArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEmpty(lookupElements)
  }

  @Test
  fun testVariableEnumType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$second", "\$variable")
    completionFixture.checkTypeText(lookupElements, "\$second", "MyEnum!")
    completionFixture.checkTypeText(lookupElements, "\$variable", "MyEnum")
  }

  @Test
  fun testVariableListTypeForNonNullArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEmpty(lookupElements)
  }

  @Test
  fun testVariableListTypeForNullableListNullableArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$four", "\$one", "\$three", "\$two")
  }

  @Test
  fun testVariableInListTypeForNullableListNullableArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$enum", "\$enum1", "\$four", "\$one", "\$three", "\$two")

    // TODO: [vepanimas] should match only inner types
    // checkEqualsOrdered(lookupElements, "$enum", "$enum1");
  }

  @Test
  fun testVariableListTypeForNonNullListNullableArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$four", "\$two")
  }

  @Test
  fun testVariableInListTypeForNonNullListNullableArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$four", "\$string1", "\$two")

    // TODO: [vepanimas] should match only inner types
    // checkEqualsOrdered(lookupElements, "$string1");
  }

  @Test
  fun testVariableListTypeForNullableListNonNullArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$four", "\$three")
  }

  @Test
  fun testVariableInListTypeForNullableListNonNullArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$four", "\$id", "\$id1", "\$three")

    // TODO: [vepanimas] should match only inner types
    // checkEqualsOrdered(lookupElements, "$id", "$id1");
  }

  @Test
  fun testVariableListTypeForNonNullListNonNullArg(): Unit = timeoutRunBlocking {
    completionFixture.doTestWithSchema()
    completionFixture.checkResult()
  }

  @Test
  fun testVariableInListTypeForNonNullListNonNullArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$four", "\$id1")

    // TODO: [vepanimas] should match only inner types - only `$id1` in that case
    // doTestWithSchema();
    // checkResult();
  }

  @Test
  fun testVariableUseInList(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$second", "\$variable")
  }

  @Test
  fun testVariableWithoutPrefix(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$var1", "\$var3", "\$var5")
  }

  @Test
  fun testVariableListTypeWithoutPrefix(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "\$var5", "\$var6")
  }

  @Test
  fun testVariableDefinitionType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements,
                                         "Boolean", "Float", "ID", "Int", "MyEnum", "MyInput",
                                         "MyNestedInput", "OtherObject", "String", "Units", "UserFilter")
    completionFixture.checkResult(lookupElements, "UserFilter")
  }

  // -- directives --
  @Test
  fun testDirectiveOnField(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "include", "onField", "skip")
  }

  @Test
  fun testDirectiveOnFieldArg(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "if")
  }

  @Test
  fun testSubscriptionOperationDirectives(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithSchema()
    completionFixture.checkEqualsOrdered(lookupElements, "SubscriptionDir", "SubscriptionDir1")
  }

  @Test
  fun testEnumArgumentInInjection(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTestWithProject(".js")
    completionFixture.checkEqualsOrdered(lookupElements, "ADMIN", "GUEST", "USER")
  }

  @Test
  fun testInputFieldAsVariableDefaultValue(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "criterion")
    completionFixture.checkResult(lookupElements, "criterion")
  }

  companion object {
    private const val OTHER_SCHEMA = "SchemaOther.graphql"
    private const val BRACES_INSERTION_SCHEMA = "SchemaBracesInsertion.graphql"
  }
}
