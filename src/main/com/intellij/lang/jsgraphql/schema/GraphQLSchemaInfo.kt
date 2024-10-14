/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.google.common.collect.Lists
import com.intellij.lang.jsgraphql.ide.validation.GraphQLErrorFilter
import com.intellij.lang.jsgraphql.types.GraphQLError
import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.types.schema.GraphQLSchema
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry
import com.intellij.lang.jsgraphql.types.schema.idl.errors.SchemaProblem
import com.intellij.lang.jsgraphql.types.schema.validation.InvalidSchemaException
import com.intellij.openapi.project.Project

class GraphQLSchemaInfo(
  val schema: GraphQLSchema,
  private val additionalErrors: List<GraphQLException>,
  private val registryInfo: GraphQLRegistryInfo,
) {
  val registry: TypeDefinitionRegistry
    get() = registryInfo.typeDefinitionRegistry

  val isTooComplex: Boolean
    get() = registryInfo.isTooComplex

  fun getErrors(project: Project): List<GraphQLError> {
    val rawErrors: MutableList<GraphQLException> = Lists.newArrayList(additionalErrors)
    rawErrors.addAll(registryInfo.errors)
    rawErrors.addAll(schema.errors)

    val errors = mutableListOf<GraphQLError>()
    if (isTooComplex) {
      errors.add(GraphQLSchemaTooComplexError())
    }

    for (exception in rawErrors) {
      when (exception) {
        is SchemaProblem -> errors.addAll(exception.errors)
        is GraphQLError -> errors.add(exception as GraphQLError)
        is InvalidSchemaException -> errors.addAll(exception.errors)
        else -> errors.add(GraphQLUnexpectedSchemaError(exception))
      }
    }

    return errors.filter { error: GraphQLError ->
      GraphQLErrorFilter.EP_NAME.extensionList.none { filter: GraphQLErrorFilter ->
        filter.isGraphQLErrorSuppressed(project, error, null)
      }
    }
  }
}
