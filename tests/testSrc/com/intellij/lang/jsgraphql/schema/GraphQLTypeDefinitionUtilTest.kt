package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.graphQLTestFixture
import com.intellij.lang.jsgraphql.types.language.ObjectTypeDefinition
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationError
import com.intellij.lang.jsgraphql.types.schema.validation.SchemaValidationErrorType
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow

@TestApplication
class GraphQLTypeDefinitionUtilTest {
  private val graphQLFixture by graphQLTestFixture("/schema")

  @Test
  fun testFindElementForErrorWithNodeWithoutSourceLocation() {
    val node = ObjectTypeDefinition.newObjectTypeDefinition().name("Foo").build()
    assertNull(node.sourceLocation, "precondition: a synthesized node has no source location")

    val error = SchemaValidationError(
      SchemaValidationErrorType.ImplementingTypeLackOfFieldError,
      "\"Foo\" must define one or more fields.",
      node,
    )

    assertDoesNotThrow {
      assertNull(error.findElement(graphQLFixture.project))
    }
  }
}
