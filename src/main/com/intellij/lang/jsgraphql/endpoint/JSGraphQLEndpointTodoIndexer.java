/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import com.intellij.lang.jsgraphql.endpoint.lexer.JSGraphQLEndpointLexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.impl.cache.impl.todo.LexerBasedTodoIndexer;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointTodoIndexer extends LexerBasedTodoIndexer {

    @Override
    public Lexer createLexer(OccurrenceConsumer consumer) {
        return new JSGraphQLEndpointTodoLexer(consumer);
    }

    private static class JSGraphQLEndpointTodoLexer extends BaseFilterLexer {

        JSGraphQLEndpointTodoLexer(OccurrenceConsumer occurrenceConsumer) {
            super(new FlexAdapter(new JSGraphQLEndpointLexer()), occurrenceConsumer);
        }

        @Override
        public void advance() {
            final IElementType tokenType = this.myDelegate.getTokenType();
            if (tokenType == JSGraphQLEndpointTokenTypes.LINE_COMMENT) {
                this.scanWordsInToken(UsageSearchContext.IN_COMMENTS, false, false);
                this.advanceTodoItemCountsInToken();
            }
            this.myDelegate.advance();
        }
    }
}
