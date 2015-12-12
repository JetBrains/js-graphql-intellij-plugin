/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JSGraphQLPsiElement extends ASTWrapperPsiElement {

    public JSGraphQLPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {

        if(getFirstChild() != null && isSupportedGraphQLReference(getFirstChild())) {
            final PsiElement self = this;
            return new PsiReferenceBase<JSGraphQLPsiElement>(this, TextRange.from(0, self.getTextLength())) {

                @Nullable
                @Override
                public PsiElement resolve() {
                    return getFirstChild() != null ? getFirstChild() : self;
                }

                @NotNull
                @Override
                public Object[] getVariants() {
                    // variants appears to filter the shown completions -- but should be okay for now inside a single property/type name
                    return new Object[0];
                }
            };
        }
        return super.getReference();

    }

    private boolean isSupportedGraphQLReference(PsiElement element) {
        if(element.getNode().getElementType() == JSGraphQLTokenTypes.PROPERTY) {
            return true;
        }
        if(element.getNode().getElementType() == JSGraphQLTokenTypes.ATOM) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isPhysical() {
        return super.isPhysical();
    }
}
