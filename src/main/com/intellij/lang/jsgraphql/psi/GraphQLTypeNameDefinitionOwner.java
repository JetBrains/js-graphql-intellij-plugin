/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.jsgraphql.psi.GraphQLElement;
import com.intellij.lang.jsgraphql.psi.GraphQLTypeNameDefinition;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by type system definitions, e.g. object/interface/input/enum/union/scalar definitions
 */
public interface GraphQLTypeNameDefinitionOwner extends GraphQLElement {

    @Nullable
    GraphQLTypeNameDefinition getTypeNameDefinition();
}
