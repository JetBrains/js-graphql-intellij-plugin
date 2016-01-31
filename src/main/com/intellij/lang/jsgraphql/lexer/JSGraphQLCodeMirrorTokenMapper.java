/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.lexer;

import com.google.common.collect.Maps;
import com.intellij.lang.jsgraphql.JSGraphQLTokenType;
import com.intellij.lang.jsgraphql.JSGraphQLTokenTypes;
import com.intellij.psi.tree.IElementType;

import java.util.Map;

public class JSGraphQLCodeMirrorTokenMapper {

    private final static Map<String, IElementType> mappings = Maps.newConcurrentMap();

    static {
        for (IElementType tokenType : JSGraphQLTokenTypes.ALL_TOKEN_TYPES) {
            if(tokenType instanceof JSGraphQLTokenType) {
                mappings.put(((JSGraphQLTokenType)tokenType).getLexerTokenType(), tokenType);
            } else if(tokenType.equals(JSGraphQLTokenTypes.WHITESPACE)) {
                mappings.put("ws", tokenType); // CodeMirror uses 'ws' for whitespace
            } else if(tokenType.equals(JSGraphQLTokenTypes.INVALIDCHAR)) {
                mappings.put("invalidchar", tokenType); // CodeMirror uses 'invalidchar' for whitespace
            }
        }
    }

    public static IElementType getTokenType(String lexerTokenType) {
        return mappings.getOrDefault(lexerTokenType, JSGraphQLTokenTypes.UNKNOWN);
    }
}
