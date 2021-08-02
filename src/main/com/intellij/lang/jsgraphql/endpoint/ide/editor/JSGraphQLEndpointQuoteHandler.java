/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.editor;

import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypesSets;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public class JSGraphQLEndpointQuoteHandler extends SimpleTokenSetQuoteHandler {

    public JSGraphQLEndpointQuoteHandler() {
        super(JSGraphQLEndpointTokenTypesSets.STRING_TOKENS);
    }

    public boolean isOpeningQuote(final HighlighterIterator iterator, final int offset) {
        return iterator.getTokenType() == JSGraphQLEndpointTokenTypes.OPEN_QUOTE;
    }

    public boolean isClosingQuote(final HighlighterIterator iterator, final int offset) {
        return iterator.getTokenType() == JSGraphQLEndpointTokenTypes.CLOSING_QUOTE;
    }

}
