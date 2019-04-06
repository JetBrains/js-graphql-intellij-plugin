/*
 * Copyright (c) 2018-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.jsgraphql.GraphQLLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class GraphQLTokenType extends IElementType {
  public GraphQLTokenType(@NotNull @NonNls String debugName) {
    super(debugName, GraphQLLanguage.INSTANCE);
  }
}
