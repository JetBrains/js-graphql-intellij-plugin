/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLInlineFragmentPsiElement extends JSGraphQLPsiElement {
    public JSGraphQLInlineFragmentPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable
    @Override
    protected Icon getElementIcon(@IconFlags int flags) {
        return JSGraphQLIcons.Schema.Fragment;
    }
}
