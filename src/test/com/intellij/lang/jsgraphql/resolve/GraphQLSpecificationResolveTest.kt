package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.*

class GraphQLSpecificationResolveTest : GraphQLResolveTestCaseBase() {
  override fun getBasePath(): String {
    return "/resolve/specification"
  }

  fun testDirectiveLocation() {
    val target = doResolveAsTextTest(GraphQLEnumValueDefinition::class.java, "FIELD_DEFINITION")
    assertContainingDefinition(target, GraphQLEnumTypeDefinition::class.java, "__DirectiveLocation")
  }

  fun testScalar() {
    doResolveAsTextTest(GraphQLScalarTypeDefinition::class.java, "String")
  }

  fun testDirective() {
    doResolveAsTextTest(GraphQLDirectiveDefinition::class.java, "deprecated")
  }

  fun testTypeName() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameNested() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameUnion() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameInterface() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameInlineFragment() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testIntrospectionType() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__type")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__QueryIntrospectionMeta")
  }

  fun testIntrospectionTypeFieldNames() {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "name")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__Field")
  }

  // TODO: [resolve] fix
  fun _testIntrospectionTypesOnRootLevelOnly() {
    doHighlightingTest()
  }
}
