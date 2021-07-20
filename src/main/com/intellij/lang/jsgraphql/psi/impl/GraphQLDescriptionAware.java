/*
 * Copyright (c) 2019-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi.impl;

import com.intellij.lang.jsgraphql.psi.GraphQLDescription;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * PSI Element which can have a description, e.g. types and fields in a schema
 */
public interface GraphQLDescriptionAware extends PsiElement {

    @Nullable
    GraphQLDescription getDescription();
}
