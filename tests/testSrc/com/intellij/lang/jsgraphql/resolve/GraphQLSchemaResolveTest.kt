package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInterfaceTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLUnionTypeDefinition
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLSchemaResolveTest : GraphQLResolveTestCaseBase() {
  override fun getBasePath(): String {
    return "/resolve/schema"
  }

  fun testGithubSchema() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testAniListSchema() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testBitQuerySchema() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testUniverseSchema() = runBlockingCancellable {
    doHighlightingTest()
  }

  fun testDirectiveArgumentObjectFieldValue() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLEnumValueDefinition::class.java, "owner")
  }

  fun testDirectiveArgumentObjectField() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "allow")
  }

  fun testDefaultArgumentObjectField() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "field")
  }

  fun testDirectiveArgumentEnumValue() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLEnumValueDefinition::class.java, "FOREVER")
  }

  fun testDefaultArgumentObjectFieldInsideArray() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "field")
  }

  fun testDefaultArgumentObjectFieldNestedCircular() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "order")
  }

  fun testObjectFieldDefinitionObjectType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Location")
  }

  fun testObjectFieldDefinitionEnumType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLEnumTypeDefinition::class.java, "CheckStatusState")
  }

  fun testObjectFieldDefinitionScalarType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLScalarTypeDefinition::class.java, "Upload")
  }

  fun testObjectFieldDefinitionUnionType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLUnionTypeDefinition::class.java, "Node")
  }

  fun testObjectFieldDefinitionInterfaceType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Node")
  }

  fun testArgumentDefinitionObjectType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Argument")
  }

  fun testArgumentDefinitionInputType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Argument")
  }

  fun testDirectiveArgument() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "fieldType")
  }

  fun testSchemaOperationQueryType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomQuery")
  }

  fun testSchemaOperationMutationType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomMutation")
  }

  fun testSchemaOperationSubscriptionType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomSubscription")
  }

  fun testSchemaExtensionOperationQueryType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomQuery")
  }

  fun testSchemaExtensionOperationMutationType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomMutation")
  }

  fun testSchemaExtensionOperationSubscriptionType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomSubscription")
  }

  fun testInterfaceFieldDefinitionObjectType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Location")
  }

  fun testInputFieldDefinitionInputType() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "LocationInput")
  }

  fun testObjectTypeImplementsInterface() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Entity")
  }

  fun testObjectTypeDirective() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "table")
  }

  fun testObjectFieldDefinitionDirective() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "field")
  }

  fun testInterfaceTypeImplementsInterface() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Node")
  }

  fun testInterfaceTypeImplementsCircular() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Named")
  }

  fun testInputObjectTypeCircular() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Example")
  }

  fun testInputObjectTypeCircularList() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Example")
  }

  fun testUnionTypeMember() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "B")
  }

  fun testUnionTypeMemberCircular() = runBlockingCancellable<Unit> {
    doResolveWithOffsetTest(GraphQLUnionTypeDefinition::class.java, "U")
  }
}
