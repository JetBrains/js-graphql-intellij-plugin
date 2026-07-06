package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.GraphQLTestCaseBase
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationError
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationErrorType

class GraphQLTypeDefinitionUtilTest : GraphQLTestCaseBase() {
  override fun getBasePath(): String = "/schema"

  fun testFindElementForErrorWithNodeWithoutSourceLocation() {
    val node = ObjectTypeDefinition.newObjectTypeDefinition().name("Foo").build()
    assertNull("precondition: a synthesized node has no source location", node.sourceLocation)

    val error = SchemaValidationError(
      SchemaValidationErrorType.ImplementingTypeLackOfFieldError,
      "\"Foo\" must define one or more fields.",
      node,
    )

    assertNoThrowable {
      assertNull(error.findElement(project))
    }
  }
}
