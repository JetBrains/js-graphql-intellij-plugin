/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeName;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by type system extensions, e.g. 'extend type'/'extend interface'/'extend input'/'extend enum'/øextend union'/'extend scalar'
 */
public interface GraphQLTypeNameExtensionOwner extends GraphQLElement {

    @Nullable
    GraphQLTypeName getTypeName();
}
