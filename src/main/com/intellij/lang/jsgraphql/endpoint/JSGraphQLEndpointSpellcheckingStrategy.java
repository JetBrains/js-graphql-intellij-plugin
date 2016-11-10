/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import com.intellij.codeInspection.SuppressionUtil;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.spellchecker.inspections.IdentifierSplitter;
import com.intellij.spellchecker.tokenizer.PsiIdentifierOwnerTokenizer;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import com.intellij.spellchecker.tokenizer.TokenizerBase;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLEndpointSpellcheckingStrategy extends SpellcheckingStrategy {

    private static final TokenizerBase<PsiElement> IDENTIFIER_TOKENIZER = new TokenizerBase<>(IdentifierSplitter.getInstance());

    @NotNull
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element instanceof PsiWhiteSpace) {
            return EMPTY_TOKENIZER;
        }
        if (element instanceof PsiNameIdentifierOwner) {
            return new PsiIdentifierOwnerTokenizer();
        }
        if (element.getParent() instanceof PsiNameIdentifierOwner) {
            return EMPTY_TOKENIZER;
        }
        if (element.getNode().getElementType() == JSGraphQLEndpointTokenTypes.IDENTIFIER) {
            return IDENTIFIER_TOKENIZER;
        }
        if (element instanceof PsiComment) {
            if (SuppressionUtil.isSuppressionComment(element)) {
                return EMPTY_TOKENIZER;
            }
            return myCommentTokenizer;
        }
        return EMPTY_TOKENIZER;
    }
}