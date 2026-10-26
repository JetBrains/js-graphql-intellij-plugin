package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.graphQLResolveTestFixture
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeDefinition
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Test

@TestApplication
class GraphQLSpecificationResolveTest {
  private val resolveFixture by graphQLResolveTestFixture("/resolve/specification")

  @Test
  fun testDirectiveLocation(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLEnumValueDefinition::class.java, "FIELD_DEFINITION")
    resolveFixture.assertContainingDefinition(target, GraphQLEnumTypeDefinition::class.java, "__DirectiveLocation")
  }

  @Test
  fun testScalar(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveAsTextTest(GraphQLScalarTypeDefinition::class.java, "String")
  }

  @Test
  fun testDirective(): Unit = timeoutRunBlocking {
    resolveFixture.doResolveAsTextTest(GraphQLDirectiveDefinition::class.java, "deprecated")
  }

  @Test
  fun testTypeName(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  @Test
  fun testTypeNameNested(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  @Test
  fun testTypeNameUnion(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  @Test
  fun testTypeNameInterface(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  @Test
  fun testTypeNameInlineFragment(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  @Test
  fun testIntrospectionType(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__type")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__QueryIntrospectionMeta")
  }

  @Test
  fun testIntrospectionTypeFieldNames(): Unit = timeoutRunBlocking {
    val target = resolveFixture.doResolveAsTextTest(GraphQLFieldDefinition::class.java, "name")
    resolveFixture.assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__Field")
  }

  // TODO: [resolve] fix
  fun _testIntrospectionTypesOnRootLevelOnly(): Unit = timeoutRunBlocking {
    resolveFixture.doHighlightingTest()
  }
}
