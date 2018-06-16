/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.psi;

import com.intellij.json.psi.impl.JSStringLiteralEscaper;
import com.intellij.lang.ASTNode;
import com.intellij.psi.LiteralTextEscaper;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.SharedPsiElementImplUtil;
import com.intellij.psi.impl.source.tree.LeafElement;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLTemplateFragmentPsiElement extends JSGraphQLPsiElement implements PsiLanguageInjectionHost {

    public JSGraphQLTemplateFragmentPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return null;
    }

    @NotNull
    @Override
    public PsiReference[] getReferences() {
        return SharedPsiElementImplUtil.getReferences(this);
    }

    @Override
    public boolean isValidHost() {
        return true;
    }

    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        ASTNode valueNode = getNode().getFirstChildNode();
        assert valueNode instanceof LeafElement;
        ((LeafElement)valueNode).replaceWithText(text);
        return this;
    }

    @NotNull
    @Override
    public LiteralTextEscaper<? extends PsiLanguageInjectionHost> createLiteralTextEscaper() {
        return new JSStringLiteralEscaper<PsiLanguageInjectionHost>(this) {
            @Override
            protected boolean isRegExpLiteral() {
                return false;
            }
        };
    }
}
