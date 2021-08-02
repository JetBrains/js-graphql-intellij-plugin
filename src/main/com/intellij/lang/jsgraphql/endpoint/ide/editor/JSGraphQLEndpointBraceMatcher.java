/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.highlighting.PairedBraceMatcherAdapter;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointLanguage;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointBraceMatcher extends PairedBraceMatcherAdapter {

    public JSGraphQLEndpointBraceMatcher() {
        super(new MyPairedBraceMatcher(), JSGraphQLEndpointLanguage.INSTANCE);
    }

    private static class MyPairedBraceMatcher implements PairedBraceMatcher {

        static final BracePair[] PAIRS = new BracePair[]{
                new BracePair(JSGraphQLEndpointTokenTypes.LPAREN, JSGraphQLEndpointTokenTypes.RPAREN, false),
                new BracePair(JSGraphQLEndpointTokenTypes.LBRACKET, JSGraphQLEndpointTokenTypes.RBRACKET, false),
                new BracePair(JSGraphQLEndpointTokenTypes.LBRACE, JSGraphQLEndpointTokenTypes.RBRACE, false)  // !! Idea inserts an extra closing RBRACE is structural true
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
