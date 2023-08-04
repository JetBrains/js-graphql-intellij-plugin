/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Implemented by type system extensions, e.g. 'extend type'/'extend interface'/'extend input'/'extend enum'/'extend union'/'extend scalar'
 */
public interface GraphQLNamedTypeExtension extends GraphQLTypeExtension {

  @Nullable
  GraphQLTypeName getTypeName();
}
