package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.graphQLResolveTestFixture
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInputValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLInterfaceTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLUnionTypeDefinition
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLSchemaResolveTest {
  private val resolveFixture by graphQLResolveTestFixture("/resolve/schema")

  @Test
  fun testGithubSchema(): Unit = timeoutRunBlocking {
    resolveFixture.doHighlightingTest()
  }

  @Test
  fun testAniListSchema(): Unit = timeoutRunBlocking {
    resolveFixture.doHighlightingTest()
  }

  @Test
  fun testBitQuerySchema(): Unit = timeoutRunBlocking {
    resolveFixture.doHighlightingTest()
  }

  @Test
  fun testUniverseSchema(): Unit = timeoutRunBlocking {
    resolveFixture.doHighlightingTest()
  }

  @Test
  fun testDirectiveArgumentObjectFieldValue(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLEnumValueDefinition::class.java, "owner")
  }

  @Test
  fun testDirectiveArgumentObjectField(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "allow")
  }

  @Test
  fun testDefaultArgumentObjectField(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "field")
  }

  @Test
  fun testDirectiveArgumentEnumValue(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLEnumValueDefinition::class.java, "FOREVER")
  }

  @Test
  fun testDefaultArgumentObjectFieldInsideArray(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "field")
  }

  @Test
  fun testDefaultArgumentObjectFieldNestedCircular(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "order")
  }

  @Test
  fun testObjectFieldDefinitionObjectType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Location")
  }

  @Test
  fun testObjectFieldDefinitionEnumType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLEnumTypeDefinition::class.java, "CheckStatusState")
  }

  @Test
  fun testObjectFieldDefinitionScalarType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLScalarTypeDefinition::class.java, "Upload")
  }

  @Test
  fun testObjectFieldDefinitionUnionType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLUnionTypeDefinition::class.java, "Node")
  }

  @Test
  fun testObjectFieldDefinitionInterfaceType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Node")
  }

  @Test
  fun testArgumentDefinitionObjectType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Argument")
  }

  @Test
  fun testArgumentDefinitionInputType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Argument")
  }

  @Test
  fun testDirectiveArgument(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputValueDefinition::class.java, "fieldType")
  }

  @Test
  fun testSchemaOperationQueryType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomQuery")
  }

  @Test
  fun testSchemaOperationMutationType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomMutation")
  }

  @Test
  fun testSchemaOperationSubscriptionType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomSubscription")
  }

  @Test
  fun testSchemaExtensionOperationQueryType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomQuery")
  }

  @Test
  fun testSchemaExtensionOperationMutationType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomMutation")
  }

  @Test
  fun testSchemaExtensionOperationSubscriptionType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "CustomSubscription")
  }

  @Test
  fun testInterfaceFieldDefinitionObjectType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "Location")
  }

  @Test
  fun testInputFieldDefinitionInputType(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "LocationInput")
  }

  @Test
  fun testObjectTypeImplementsInterface(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Entity")
  }

  @Test
  fun testObjectTypeDirective(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "table")
  }

  @Test
  fun testObjectFieldDefinitionDirective(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLDirectiveDefinition::class.java, "field")
  }

  @Test
  fun testInterfaceTypeImplementsInterface(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Node")
  }

  @Test
  fun testInterfaceTypeImplementsCircular(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInterfaceTypeDefinition::class.java, "Named")
  }

  @Test
  fun testInputObjectTypeCircular(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Example")
  }

  @Test
  fun testInputObjectTypeCircularList(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLInputObjectTypeDefinition::class.java, "Example")
  }

  @Test
  fun testUnionTypeMember(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLObjectTypeDefinition::class.java, "B")
  }

  @Test
  fun testUnionTypeMemberCircular(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveWithOffsetTest(GraphQLUnionTypeDefinition::class.java, "U")
  }
}
