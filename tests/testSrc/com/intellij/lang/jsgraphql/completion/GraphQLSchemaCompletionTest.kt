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
class GraphQLSchemaCompletionTest {
  private val completionFixture by graphQLCompletionTestFixture("/completion/schema")

  @Test
  fun testSchemaOperationTypes(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "CustomMutation", "ObjectType")
  }

  @Test
  fun testExtendSchema(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    assertNotNull(lookupElements)
    assertThat(lookupElements!!).isEmpty()
  }

  @Test
  fun testExtendObjectType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Mutation", "Organization",
                                         "OrganizationsListResponse", "Query", "TeamMember", "TeamMembersListResponse")
    completionFixture.checkResult(lookupElements, "TeamMember")
  }

  @Test
  fun testExtendInterfaceType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Entity", "NamedNode", "Node")
    completionFixture.checkResult(lookupElements, "NamedNode")
  }

  @Test
  fun testExtendUnionType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Object", "ResponseTypes")
    completionFixture.checkResult(lookupElements, "ResponseTypes")
  }

  @Test
  fun testExtendScalarType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Boolean", "Date", "DateTime", "Float", "ID", "Int", "String")
    completionFixture.checkResult(lookupElements, "DateTime")
  }

  @Test
  fun testExtendInputType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements,
                                         "OrganizationInput", "OrganizationInputFilter", "TeamMemberInput", "TeamMemberInputFilter")
    completionFixture.checkResult(lookupElements, "TeamMemberInput")
  }

  @Test
  fun testExtendEnumType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "SortOrder", "TeamMemberRole", "TeamMemberStatus")
    completionFixture.checkResult(lookupElements, "TeamMemberRole")
  }

  @Test
  fun testFieldType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface",
                                         "KnownType", "MyEnum", "MyUnion", "String")
  }

  @Test
  fun testFieldTypeListRequired(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface",
                                         "KnownType", "MyEnum", "MyUnion", "String")
  }

  @Test
  fun testUnionTypeMembers(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "OrganizationsListResponse", "TeamMember", "TeamMembersListResponse")
    completionFixture.checkResult(lookupElements, "TeamMember")
  }

  @Test
  fun testImplementsFirstInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "KnownInterface1", "KnownInterface2")
    completionFixture.checkResult(lookupElements, "KnownInterface1")
  }

  @Test
  fun testImplementsSecondInterface(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "KnownInterface2")
    completionFixture.checkResult(lookupElements, "KnownInterface2")
  }

  @Test
  fun testDirectiveLocations(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "ARGUMENT_DEFINITION", "ENUM", "ENUM_VALUE", "FIELD", "FIELD_DEFINITION",
                                         "FRAGMENT_DEFINITION", "FRAGMENT_SPREAD", "INLINE_FRAGMENT", "INPUT_FIELD_DEFINITION",
                                         "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT", "QUERY",
                                         "SCALAR", "SCHEMA", "SUBSCRIPTION", "UNION", "VARIABLE_DEFINITION")
  }

  @Test
  fun testDirectiveLocations1(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "ENUM", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION", "FRAGMENT_SPREAD",
                                         "INLINE_FRAGMENT", "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT",
                                         "SCALAR", "SCHEMA", "SUBSCRIPTION", "UNION",
                                         "VARIABLE_DEFINITION")
  }

  @Test
  fun testFieldOverride(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "fieldToImpl2(id: ID): Boolean", "fieldToImpl3: String")
    completionFixture.checkResult(lookupElements, "fieldToImpl2(id: ID): Boolean")
  }

  @Test
  fun testFieldOverrideInterfaceExtension(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "createdAt: DateTime")

    // TODO: [vepanimas] currently ignores interface extensions
    // checkEqualsOrdered(lookupElements, "createdAt: DateTime", "updatedAt: DateTime");
  }

  @Test
  fun testInputFieldType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInput", "MyEnum", "MyInput1",
                                         "String")
    completionFixture.checkResult(lookupElements, "MyCompletionInput")
  }

  @Test
  fun testArgumentType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String")
    completionFixture.checkResult(lookupElements, "MyCompletionInputABC")
  }

  @Test
  fun testSecondArgumentType(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String")
  }

  @Test
  fun testDirectiveNames1(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "deprecated", "foo", "foo1")
    completionFixture.checkResult(lookupElements, "foo")
  }

  @Test
  fun testDirectiveNames2(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "deprecated", "foo")
    completionFixture.checkResult(lookupElements, "deprecated")
  }

  @Test
  fun testDirectiveNamesScalar(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "specifiedBy")
    completionFixture.checkResult(lookupElements, "specifiedBy")
  }

  @Test
  fun testDirectiveNamesRequiredArgs(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "deprecated", "foo", "foo1")
    completionFixture.checkResult(lookupElements, "foo1")
  }

  fun _testDirectiveNamesScalarBeforeItsDeclaration(): Unit = timeoutRunBlocking {
    // TODO: [vepanimas] fix parser recovery for not completed top level definitions
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "specifiedBy", "required", "optional")
    completionFixture.checkResult(lookupElements, "optional")
  }

  @Test
  fun testDirectiveArguments(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "arg", "id", "s")
    completionFixture.checkResult(lookupElements, "id")
  }

  fun _testDirectiveBooleanArgumentValues(): Unit = timeoutRunBlocking {
    // TODO: [vepanimas] currently not supported by the completion contributor
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "false", "true")
    completionFixture.checkResult(lookupElements, "false")
  }

  @Test
  fun testSchemaDirectives(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "DirSchema", "DirSchemaRepeatable")
  }

  @Test
  fun testSchemaExtensionDirectives(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "DirSchema", "DirSchemaRepeatable")
  }

  @Test
  fun testDefaultArgumentObjectValue(): Unit = timeoutRunBlocking {
    val lookupElements = completionFixture.doTest()
    completionFixture.checkEqualsOrdered(lookupElements, "ASC", "DESC")
  }
}
