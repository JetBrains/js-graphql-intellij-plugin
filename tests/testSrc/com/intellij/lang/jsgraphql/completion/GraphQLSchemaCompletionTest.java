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


public class GraphQLSchemaCompletionTest extends GraphQLCompletionTestCaseBase {

  @Override
  protected @NotNull String getBasePath() {
    return "/completion/schema";
  }

  public void testSchemaOperationTypes() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "CustomMutation", "ObjectType");
  }

  public void testExtendSchema() {
    LookupElement[] lookupElements = doTest();
    assertNotNull(lookupElements);
    assertEmpty(lookupElements);
  }

  public void testExtendObjectType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Mutation", "Organization",
                       "OrganizationsListResponse", "Query", "TeamMember", "TeamMembersListResponse");
    checkResult(lookupElements, "TeamMember");
  }

  public void testExtendInterfaceType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Entity", "NamedNode", "Node");
    checkResult(lookupElements, "NamedNode");
  }

  public void testExtendUnionType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Object", "ResponseTypes");
    checkResult(lookupElements, "ResponseTypes");
  }

  public void testExtendScalarType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Boolean", "Date", "DateTime", "Float", "ID", "Int", "String");
    checkResult(lookupElements, "DateTime");
  }

  public void testExtendInputType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements,
                       "OrganizationInput", "OrganizationInputFilter", "TeamMemberInput", "TeamMemberInputFilter");
    checkResult(lookupElements, "TeamMemberInput");
  }

  public void testExtendEnumType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "SortOrder", "TeamMemberRole", "TeamMemberStatus");
    checkResult(lookupElements, "TeamMemberRole");
  }

  public void testFieldType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType",
                       "MyEnum", "MyUnion", "String");
  }

  public void testFieldTypeListRequired() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "AnotherKnownType", "Boolean", "Float", "Foo", "ID", "Int", "KnownInterface", "KnownType",
                       "MyEnum", "MyUnion", "String");
  }

  public void testUnionTypeMembers() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "OrganizationsListResponse", "TeamMember", "TeamMembersListResponse");
    checkResult(lookupElements, "TeamMember");
  }

  public void testImplementsFirstInterface() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "KnownInterface1", "KnownInterface2");
    checkResult(lookupElements, "KnownInterface1");
  }

  public void testImplementsSecondInterface() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "KnownInterface2");
    checkResult(lookupElements, "KnownInterface2");
  }

  public void testDirectiveLocations() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "ARGUMENT_DEFINITION", "ENUM", "ENUM_VALUE", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION",
                       "FRAGMENT_SPREAD", "INLINE_FRAGMENT", "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT",
                       "QUERY",
                       "SCALAR", "SCHEMA", "SUBSCRIPTION", "UNION", "VARIABLE_DEFINITION");
  }

  public void testDirectiveLocations1() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "ENUM", "FIELD", "FIELD_DEFINITION", "FRAGMENT_DEFINITION", "FRAGMENT_SPREAD", "INLINE_FRAGMENT",
                       "INPUT_FIELD_DEFINITION", "INPUT_OBJECT", "INTERFACE", "MUTATION", "OBJECT", "SCALAR", "SCHEMA", "SUBSCRIPTION",
                       "UNION",
                       "VARIABLE_DEFINITION");
  }

  public void testFieldOverride() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "fieldToImpl2(id: ID): Boolean", "fieldToImpl3: String");
    checkResult(lookupElements, "fieldToImpl2(id: ID): Boolean");
  }

  public void testFieldOverrideInterfaceExtension() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "createdAt: DateTime");

    // TODO: [vepanimas] currently ignores interface extensions
    // checkEqualsOrdered(lookupElements, "createdAt: DateTime", "updatedAt: DateTime");
  }

  public void testInputFieldType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInput", "MyEnum", "MyInput1", "String");
    checkResult(lookupElements, "MyCompletionInput");
  }

  public void testArgumentType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String");
    checkResult(lookupElements, "MyCompletionInputABC");
  }

  public void testSecondArgumentType() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "Boolean", "Float", "ID", "Int", "MyCompletionInputABC", "MyEnum", "String");
  }

  public void testDirectiveNames1() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "deprecated", "foo", "foo1");
    checkResult(lookupElements, "foo");
  }

  public void testDirectiveNames2() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "deprecated", "foo");
    checkResult(lookupElements, "deprecated");
  }

  public void testDirectiveNamesScalar() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "specifiedBy");
    checkResult(lookupElements, "specifiedBy");
  }

  public void testDirectiveNamesRequiredArgs() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "deprecated", "foo", "foo1");
    checkResult(lookupElements, "foo1");
  }

  public void _testDirectiveNamesScalarBeforeItsDeclaration() {
    // TODO: [vepanimas] fix parser recovery for not completed top level definitions
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "specifiedBy", "required", "optional");
    checkResult(lookupElements, "optional");
  }

  public void testDirectiveArguments() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "arg", "id", "s");
    checkResult(lookupElements, "id");
  }

  public void _testDirectiveBooleanArgumentValues() {
    // TODO: [vepanimas] currently not supported by the completion contributor
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "false", "true");
    checkResult(lookupElements, "false");
  }

  public void testSchemaDirectives() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "DirSchema", "DirSchemaRepeatable");
  }

  public void testSchemaExtensionDirectives() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "DirSchema", "DirSchemaRepeatable");
  }

  public void testDefaultArgumentObjectValue() {
    LookupElement[] lookupElements = doTest();
    checkEqualsOrdered(lookupElements, "ASC", "DESC");
  }
}
