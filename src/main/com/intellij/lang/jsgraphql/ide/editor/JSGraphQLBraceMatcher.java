/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.jsgraphql.JSGraphQLLanguage;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class JSGraphQLBraceMatcher extends PairedBraceMatcherAdapter {

    public JSGraphQLBraceMatcher() {
        super(new MyPairedBraceMatcher(), JSGraphQLLanguage.INSTANCE);
    }

    private static class MyPairedBraceMatcher implements PairedBraceMatcher {

        static final BracePair[] PAIRS = new BracePair[]{
                new BracePair(JSGraphQLTokenTypes.LPAREN, JSGraphQLTokenTypes.RPAREN, true),
                new BracePair(JSGraphQLTokenTypes.LBRACKET, JSGraphQLTokenTypes.RBRACKET, true),
                new BracePair(JSGraphQLTokenTypes.LBRACE, JSGraphQLTokenTypes.RBRACE, true)
        };

        @Override public BracePair[] getPairs() {
            return PAIRS;
        }

        @Override public boolean isPairedBracesAllowedBeforeType(@NotNull IElementType lbraceType, IElementType contextType) {
            return true;
        }

        @Override public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
            return openingBraceOffset;
        }
    }


}
