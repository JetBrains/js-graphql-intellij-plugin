package com.intellij.lang.jsgraphql.resolve

import com.intellij.lang.jsgraphql.GraphQLResolveTestCaseBase
import com.intellij.lang.jsgraphql.psi.GraphQLDirectiveDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLEnumValueDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLFieldDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLObjectTypeDefinition
import com.intellij.lang.jsgraphql.psi.GraphQLScalarTypeDefinition
import com.intellij.openapi.progress.runBlockingCancellable

class GraphQLSpecificationResolveTest : GraphQLResolveTestCaseBase() {
  override fun getBasePath(): String {
    return "/resolve/specification"
  }

  fun testDirectiveLocation() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLEnumValueDefinition::class.java, "FIELD_DEFINITION")
    assertContainingDefinition(target, GraphQLEnumTypeDefinition::class.java, "__DirectiveLocation")
  }

  fun testScalar() = runBlockingCancellable<Unit> {
    doResolveAsTextTest(GraphQLScalarTypeDefinition::class.java, "String")
  }

  fun testDirective() = runBlockingCancellable<Unit> {
    doResolveAsTextTest(GraphQLDirectiveDefinition::class.java, "deprecated")
  }

  fun testTypeName() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameNested() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameUnion() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameInterface() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testTypeNameInlineFragment() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__typename")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__TypeNameMeta")
  }

  fun testIntrospectionType() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "__type")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__QueryIntrospectionMeta")
  }

  fun testIntrospectionTypeFieldNames() = runBlockingCancellable {
    val target = doResolveAsTextTest(GraphQLFieldDefinition::class.java, "name")
    assertContainingDefinition(target, GraphQLObjectTypeDefinition::class.java, "__Field")
  }

  // TODO: [resolve] fix
  fun _testIntrospectionTypesOnRootLevelOnly() = runBlockingCancellable {
    doHighlightingTest()
  }
}
