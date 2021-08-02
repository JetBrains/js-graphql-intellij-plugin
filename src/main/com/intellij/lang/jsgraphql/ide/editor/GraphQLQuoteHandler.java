/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.lang.jsgraphql.psi.GraphQLElementTypes;
import com.intellij.lang.jsgraphql.psi.GraphQLExtendedElementTypes;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public class GraphQLQuoteHandler extends SimpleTokenSetQuoteHandler {

    public GraphQLQuoteHandler() {
        super(GraphQLExtendedElementTypes.SINGLE_QUOTES);
    }

    public boolean isOpeningQuote(final HighlighterIterator iterator, final int offset) {
        return iterator.getTokenType() == GraphQLElementTypes.OPEN_QUOTE;
    }

    public boolean isClosingQuote(final HighlighterIterator iterator, final int offset) {
        return iterator.getTokenType() == GraphQLElementTypes.CLOSING_QUOTE;
    }

}
