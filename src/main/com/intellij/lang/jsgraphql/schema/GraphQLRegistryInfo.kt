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
  val isTooComplex: Boolean = false,
) {
  val errors: List<GraphQLException>
    get() = typeDefinitionRegistry.errors
}
