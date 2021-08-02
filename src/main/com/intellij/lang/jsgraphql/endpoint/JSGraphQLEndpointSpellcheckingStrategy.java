/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLEndpointSpellcheckingStrategy extends SpellcheckingStrategy {

    private static final TokenizerBase<PsiElement> IDENTIFIER_TOKENIZER = new TokenizerBase<>(IdentifierSplitter.getInstance());

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element.getParent() instanceof PsiNameIdentifierOwner) {
            return EMPTY_TOKENIZER;
        }
        if (element.getNode().getElementType() == JSGraphQLEndpointTokenTypes.IDENTIFIER) {
            return IDENTIFIER_TOKENIZER;
        }
        return super.getTokenizer(element);
    }
}
