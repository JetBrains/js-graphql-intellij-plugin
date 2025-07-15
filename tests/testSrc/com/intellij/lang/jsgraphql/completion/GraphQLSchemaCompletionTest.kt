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

class GraphQLSchemaCompletionTest : GraphQLCompletionTestCaseBase() {
  override fun getBasePath(): String {
    return "/completion/schema"
  }

  fun testSchemaOperationTypes() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "CustomMutation", "ObjectType")
  }

  fun testExtendSchema() = runBlockingCancellable {
    val lookupElements = doTest()
    assertNotNull(lookupElements)
    assertEmpty(lookupElements!!)
  }

  fun testExtendObjectType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Mutation", "Organization",
                       "OrganizationsListResponse", "Query", "TeamMember", "TeamMembersListResponse")
    checkResult(lookupElements, "TeamMember")
  }

  fun testExtendInterfaceType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Entity", "NamedNode", "Node")
    checkResult(lookupElements, "NamedNode")
  }

  fun testExtendUnionType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Object", "ResponseTypes")
    checkResult(lookupElements, "ResponseTypes")
  }

  fun testExtendScalarType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Boolean", "Date", "DateTime", "Float", "ID", "Int", "String")
    checkResult(lookupElements, "DateTime")
  }

  fun testExtendInputType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements,
                       "OrganizationInput", "OrganizationInputFilter", "TeamMemberInput", "TeamMemberInputFilter")
    checkResult(lookupElements, "TeamMemberInput")
  }

  fun testExtendEnumType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "SortOrder", "TeamMemberRole", "TeamMemberStatus")
    checkResult(lookupElements, "TeamMemberRole")
  }

  fun testFieldType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType",
                       "MyEnum", "MyUnion", "String")
  }

  fun testFieldTypeListRequired() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType",
                       "MyEnum", "MyUnion", "String")
  }

  fun testUnionTypeMembers() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "OrganizationsListResponse", "TeamMember", "TeamMembersListResponse")
    checkResult(lookupElements, "TeamMember")
  }

  fun testImplementsFirstInterface() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "KnownInterface1", "KnownInterface2")
    checkResult(lookupElements, "KnownInterface1")
  }

  fun testImplementsSecondInterface() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "KnownInterface2")
    checkResult(lookupElements, "KnownInterface2")
  }

  fun testDirectiveLocations() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "ARGUMENT_DEFINITION", "ENUM", "ENUM_VALUE", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION",
                       "FRAGMENT_SPREAD", "INLINE_FRAGMENT", "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT",
                       "QUERY",
                       "SCALAR", "SCHEMA", "SUBSCRIPTION", "UNION", "VARIABLE_DEFINITION")
  }

  fun testDirectiveLocations1() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "ENUM", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION", "FRAGMENT_SPREAD", "INLINE_FRAGMENT",
                       "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT", "SCALAR", "SCHEMA", "SUBSCRIPTION",
                       "UNION",
                       "VARIABLE_DEFINITION")
  }

  fun testFieldOverride() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "fieldToImpl2(id: ID): Boolean", "fieldToImpl3: String")
    checkResult(lookupElements, "fieldToImpl2(id: ID): Boolean")
  }

  fun testFieldOverrideInterfaceExtension() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "createdAt: DateTime")

    // TODO: [vepanimas] currently ignores interface extensions
    // checkEqualsOrdered(lookupElements, "createdAt: DateTime", "updatedAt: DateTime");
  }

  fun testInputFieldType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInput", "MyEnum", "MyInput1", "String")
    checkResult(lookupElements, "MyCompletionInput")
  }

  fun testArgumentType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String")
    checkResult(lookupElements, "MyCompletionInputABC")
  }

  fun testSecondArgumentType() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String")
  }

  fun testDirectiveNames1() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "deprecated", "foo", "foo1")
    checkResult(lookupElements, "foo")
  }

  fun testDirectiveNames2() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "deprecated", "foo")
    checkResult(lookupElements, "deprecated")
  }

  fun testDirectiveNamesScalar() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "specifiedBy")
    checkResult(lookupElements, "specifiedBy")
  }

  fun testDirectiveNamesRequiredArgs() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "deprecated", "foo", "foo1")
    checkResult(lookupElements, "foo1")
  }

  fun _testDirectiveNamesScalarBeforeItsDeclaration() = runBlockingCancellable {
    // TODO: [vepanimas] fix parser recovery for not completed top level definitions
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "specifiedBy", "required", "optional")
    checkResult(lookupElements, "optional")
  }

  fun testDirectiveArguments() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "arg", "id", "s")
    checkResult(lookupElements, "id")
  }

  fun _testDirectiveBooleanArgumentValues() = runBlockingCancellable {
    // TODO: [vepanimas] currently not supported by the completion contributor
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "false", "true")
    checkResult(lookupElements, "false")
  }

  fun testSchemaDirectives() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "DirSchema", "DirSchemaRepeatable")
  }

  fun testSchemaExtensionDirectives() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "DirSchema", "DirSchemaRepeatable")
  }

  fun testDefaultArgumentObjectValue() = runBlockingCancellable {
    val lookupElements = doTest()
    checkEqualsOrdered(lookupElements, "ASC", "DESC")
  }
}
