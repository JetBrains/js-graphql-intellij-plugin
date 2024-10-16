package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.GraphQLBundle
import com.intellij.lang.jsgraphql.types.ErrorClassification
import com.intellij.lang.jsgraphql.types.ErrorType
import com.intellij.lang.jsgraphql.types.GraphQLError
import com.intellij.lang.jsgraphql.types.language.SourceLocation

class GraphQLSchemaTooComplexError() : GraphQLError {
  override fun getMessage(): String = GraphQLBundle.message("graphql.schema.is.too.complex.error.text", SCHEMA_SIZE_DEFINITIONS_LIMIT)

  override fun getLocations(): List<SourceLocation> = emptyList()

  override fun getErrorType(): ErrorClassification = ErrorType.ValidationError
}