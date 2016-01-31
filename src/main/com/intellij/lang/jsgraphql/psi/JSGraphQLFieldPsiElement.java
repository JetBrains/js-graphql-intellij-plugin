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
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLFieldPsiElement extends JSGraphQLPsiElement {
    public JSGraphQLFieldPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final JSGraphQLNamedPropertyPsiElement nameElement = getNameElement();
        return nameElement != null ? nameElement.getName() : null;
    }

    public JSGraphQLNamedPropertyPsiElement getNameElement() {
        return PsiTreeUtil.getChildOfType(this, JSGraphQLNamedPropertyPsiElement.class);
    }

    @Nullable
    @Override
    protected Icon getElementIcon(@IconFlags int flags) {
        return JSGraphQLIcons.Schema.Field;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {

            @Override
            public String getPresentableText() {
                return getName();
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Icon getIcon(boolean open) {
                return JSGraphQLIcons.Schema.Field;
            }
        };
    }

}
