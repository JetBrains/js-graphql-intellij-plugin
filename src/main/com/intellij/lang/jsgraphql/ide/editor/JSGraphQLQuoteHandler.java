/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.ide.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public class JSGraphQLQuoteHandler extends SimpleTokenSetQuoteHandler {

    public JSGraphQLQuoteHandler() {
        super(JSGraphQLTokenTypes.OPEN_QUOTE, JSGraphQLTokenTypes.CLOSE_QUOTE);
    }

    public boolean isOpeningQuote(final HighlighterIterator iterator, final int offset) {
        return iterator.getTokenType() == JSGraphQLTokenTypes.OPEN_QUOTE;
    }

    public boolean isClosingQuote(final HighlighterIterator iterator, final int offset) {
        return iterator.getTokenType() == JSGraphQLTokenTypes.CLOSE_QUOTE;
    }

}
