/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.jsgraphql.types.schema.GraphQLType;
import org.jetbrains.annotations.Nullable;

public interface GraphQLTypeScopeProvider extends GraphQLElement {

  /**
   * Get the logical type that this Psi Element exposes for children, e.g. the query type for queries, and the output type for fields.
   * For fragments the type is the type condition.
   */
  @Nullable
  GraphQLType getTypeScope();
}
