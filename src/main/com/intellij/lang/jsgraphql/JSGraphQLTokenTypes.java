/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql;

import com.google.common.collect.Lists;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

public interface JSGraphQLTokenTypes {

    IElementType WHITESPACE = TokenType.WHITE_SPACE; //;"whitespace";
    IElementType INVALIDCHAR = TokenType.BAD_CHARACTER; //;"invalidchar"; e.g. invalid/unexpected characters

    IElementType KEYWORD = new JSGraphQLTokenType("KEYWORD"); //"keyword";
    IElementType PUNCTUATION = new JSGraphQLTokenType("PUNCTUATION"); //"punctuation";
    IElementType PROPERTY = new JSGraphQLTokenType("PROPERTY"); //;"property";
    IElementType DEF = new JSGraphQLTokenType("DEF"); //;"def";
    IElementType ATOM = new JSGraphQLTokenType("ATOM"); //;"atom";
    IElementType ATTRIBUTE = new JSGraphQLTokenType("ATTRIBUTE"); //;"attribute";
    IElementType VARIABLE = new JSGraphQLTokenType("VARIABLE"); //;"variable";
    IElementType QUALIFIER = new JSGraphQLTokenType("QUALIFIER"); //;"qualifier";
    IElementType NUMBER = new JSGraphQLTokenType("NUMBER"); //;"number"; e.g. 10, 10.1
    IElementType STRING = new JSGraphQLTokenType("STRING"); //;"string"; e.g. "abc", 'abc'
    IElementType BUILTIN = new JSGraphQLTokenType("BUILTIN"); //;"builtin"; e.g. true, false
    IElementType COMMENT = new JSGraphQLTokenType("COMMENT"); //;"comment"; e.g. #mycomment
    IElementType META = new JSGraphQLTokenType("META"); //;"meta"; e.g. @ in a directive
    IElementType TEMPLATE_FRAGMENT = new JSGraphQLTokenType("TEMPLATE-FRAGMENT"); //;"template-fragment"; e.g. ${Component.getFragment('foo')}

    IElementType LPAREN = new JSGraphQLTokenType("LPAREN");
    IElementType RPAREN = new JSGraphQLTokenType("RPAREN");
    IElementType LBRACE = new JSGraphQLTokenType("LBRACE");
    IElementType RBRACE = new JSGraphQLTokenType("RBRACE");
    IElementType LBRACKET= new JSGraphQLTokenType("LBRACKET");
    IElementType RBRACKET = new JSGraphQLTokenType("RBRACKET");
    IElementType OPEN_QUOTE = new JSGraphQLTokenType("OPEN_QUOTE");
    IElementType CLOSE_QUOTE = new JSGraphQLTokenType("CLOSE_QUOTE");

    IElementType UNKNOWN = new JSGraphQLTokenType("<UNKNOWN>"); // new kind of token we don't know about

    Iterable<IElementType> ALL_TOKEN_TYPES = Lists.newArrayList(
            KEYWORD,
            PUNCTUATION,
            PROPERTY,
            DEF,
            ATOM,
            WHITESPACE,
            ATTRIBUTE,
            VARIABLE,
            QUALIFIER,
            NUMBER,
            STRING,
            BUILTIN,
            COMMENT,
            INVALIDCHAR,
            LPAREN,
            RPAREN,
            LBRACE,
            RBRACE,
            LBRACKET,
            RBRACKET,
            OPEN_QUOTE,
            CLOSE_QUOTE,
            META,
            TEMPLATE_FRAGMENT
    );

}
