/**
 * Copyright (c) 2015, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.icons.JSGraphQLIcons;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class JSGraphQLFragmentDefinitionPsiElement extends JSGraphQLPsiElement {
    public JSGraphQLFragmentDefinitionPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        final JSGraphQLNamedTypePsiElement nameElement = PsiTreeUtil.getChildOfType(this, JSGraphQLNamedTypePsiElement.class);
        if(nameElement != null) {
            return nameElement.getName();
        }
        return super.getName();
    }

    public JSGraphQLNamedTypePsiElement getFragmentOnType() {
        final JSGraphQLNamedTypePsiElement[] typeElements = PsiTreeUtil.getChildrenOfType(this, JSGraphQLNamedTypePsiElement.class);
        if(typeElements != null) {
            for (JSGraphQLNamedTypePsiElement typeElement : typeElements) {
                if(typeElement.isAtom()) {
                    return typeElement;
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    protected Icon getElementIcon(@IconFlags int flags) {
        return JSGraphQLIcons.Schema.Fragment;
    }
}
