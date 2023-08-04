/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.schema

import com.intellij.lang.jsgraphql.types.GraphQLException
import com.intellij.lang.jsgraphql.types.schema.idl.TypeDefinitionRegistry

class GraphQLRegistryInfo(
  val typeDefinitionRegistry: TypeDefinitionRegistry,
  private val additionalErrors: List<GraphQLException>,
) {
  val errors: List<GraphQLException>
    get() {
      val errors = mutableListOf<GraphQLException>()
      errors.addAll(typeDefinitionRegistry.errors)
      errors.addAll(additionalErrors)
      return errors
    }
}
