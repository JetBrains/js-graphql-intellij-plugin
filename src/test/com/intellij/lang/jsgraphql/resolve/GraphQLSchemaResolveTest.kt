package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.*

class GraphQLSchemaResolveTest : GraphQLResolveTestCaseBase() {
  override fun getBasePath(): String {
    return "/resolve/schema"
  }

  fun testGithubSchema() {
    doHighlightingTest()
  }

  fun testAniListSchema() {
    doHighlightingTest()
  }

  fun testBitQuerySchema() {
    doHighlightingTest()
  }

  fun testUniverseSchema() {
    doHighlightingTest()
  }

  fun testDirectiveArgumentObjectFieldValue() {
    doResolveWithOffsetTest(GraphQLEnumValueDefinition::class.java, "owner")
  }

  fun testDirectiveArgumentObjectField() {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "allow")
  }

  fun testDefaultArgumentObjectField() {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "field")
  }

  fun testDirectiveArgumentEnumValue() {
    doResolveWithOffsetTest(GraphQLEnumValueDefinition::class.java, "FOREVER")
  }

  fun testDefaultArgumentObjectFieldInsideArray() {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "field")
  }

  fun testDefaultArgumentObjectFieldNestedCircular() {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "order")
  }

  fun testObjectFieldDefinitionObjectType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Location")
  }

  fun testObjectFieldDefinitionEnumType() {
    doResolveWithOffsetTest(GraphQLEnumTypeDefinition::class.java, "CheckStatusState")
  }

  fun testObjectFieldDefinitionScalarType() {
    doResolveWithOffsetTest(GraphQLScalarTypeDefinition::class.java, "Upload")
  }

  fun testObjectFieldDefinitionUnionType() {
    doResolveWithOffsetTest(GraphQLUnionTypeDefinition::class.java, "Node")
  }

  fun testObjectFieldDefinitionInterfaceType() {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Node")
  }

  fun testArgumentDefinitionObjectType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Argument")
  }

  fun testArgumentDefinitionInputType() {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Argument")
  }

  fun testDirectiveArgument() {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "fieldType")
  }

  fun testSchemaOperationQueryType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomQuery")
  }

  fun testSchemaOperationMutationType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomMutation")
  }

  fun testSchemaOperationSubscriptionType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomSubscription")
  }

  fun testSchemaExtensionOperationQueryType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomQuery")
  }

  fun testSchemaExtensionOperationMutationType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomMutation")
  }

  fun testSchemaExtensionOperationSubscriptionType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomSubscription")
  }

  fun testInterfaceFieldDefinitionObjectType() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Location")
  }

  fun testInputFieldDefinitionInputType() {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "LocationInput")
  }

  fun testObjectTypeImplementsInterface() {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Entity")
  }

  fun testObjectTypeDirective() {
    doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "table")
  }

  fun testObjectFieldDefinitionDirective() {
    doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "field")
  }

  fun testInterfaceTypeImplementsInterface() {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Node")
  }

  fun testInterfaceTypeImplementsCircular() {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Named")
  }

  fun testInputObjectTypeCircular() {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Example")
  }

  fun testInputObjectTypeCircularList() {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Example")
  }

  fun testUnionTypeMember() {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "B")
  }

  fun testUnionTypeMemberCircular() {
    doResolveWithOffsetTest(GraphQLUnionTypeDefinition::class.java, "U")
  }
}
