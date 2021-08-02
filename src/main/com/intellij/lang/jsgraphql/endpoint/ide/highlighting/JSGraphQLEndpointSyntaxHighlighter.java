/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.highlighting;

import java.util.HashMap;
import java.util.Map;

import com.intellij.lang.javascript.highlighting.JSHighlighter;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypesSets;
import com.intellij.lang.jsgraphql.endpoint.lexer.JSGraphQLEndpointLexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class JSGraphQLEndpointSyntaxHighlighter extends SyntaxHighlighterBase {

	private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

	public static final TextAttributesKey KEYWORD = createTextAttributesKey("JSGRAPHQL.KEYWORD", JSHighlighter.JS_KEYWORD);
	public static final TextAttributesKey PUNCTUATION = createTextAttributesKey("JSGRAPHQL.PUNCTUATION", TextAttributesKey.find("JS.COMMA"));
	public static final TextAttributesKey PAREN = createTextAttributesKey("JSGRAPHQL.PAREN", TextAttributesKey.find("JS.PARENTHS"));
	public static final TextAttributesKey BRACE = createTextAttributesKey("JSGRAPHQL.BRACE", TextAttributesKey.find("JS.BRACES"));
	public static final TextAttributesKey BRACKET = createTextAttributesKey("JSGRAPHQL.BRACKET", TextAttributesKey.find("JS.BRACKETS"));
	public static final TextAttributesKey PROPERTY = createTextAttributesKey("JSGRAPHQL.PROPERTY", TextAttributesKey.find("JS.INSTANCE_MEMBER_VARIABLE"));
	public static final TextAttributesKey DEF = createTextAttributesKey("JSGRAPHQL.DEF", TextAttributesKey.find("JS.GLOBAL_FUNCTION"));
	public static final TextAttributesKey ATTRIBUTE = createTextAttributesKey("JSGRAPHQL.ATTRIBUTE", TextAttributesKey.find("JS.PARAMETER"));
	public static final TextAttributesKey VARIABLE = createTextAttributesKey("JSGRAPHQL.VARIABLE", TextAttributesKey.find("JS.INSTANCE_MEMBER_VARIABLE"));
	public static final TextAttributesKey QUALIFIER = createTextAttributesKey("JSGRAPHQL.QUALIFIER", TextAttributesKey.find("JS.GLOBAL_FUNCTION"));
	public static final TextAttributesKey NUMBER = createTextAttributesKey("JSGRAPHQL.NUMBER", TextAttributesKey.find("JS.NUMBER"));
	public static final TextAttributesKey STRING = createTextAttributesKey("JSGRAPHQL.STRING", TextAttributesKey.find("JS.STRING"));
	public static final TextAttributesKey BUILTIN = createTextAttributesKey("JSGRAPHQL.BUILTIN", TextAttributesKey.find("JS.KEYWORD"));
	public static final TextAttributesKey COMMENT = createTextAttributesKey("JSGRAPHQL.COMMENT", TextAttributesKey.find("JS.LINE_COMMENT"));
	public static final TextAttributesKey ATOM = createTextAttributesKey("JSGRAPHQL.ATOM", TextAttributesKey.find("JS.CLASS"));
	public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey("JSGRAPHQL.BADCHARACTER", TextAttributesKey.find("JS.BADCHARACTER"));

	public static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};

	static {
		fillMap(ATTRIBUTES, JSGraphQLEndpointTokenTypesSets.KEYWORDS, KEYWORD);
		fillMap(ATTRIBUTES, JSGraphQLEndpointTokenTypesSets.PUNCTUATION, PUNCTUATION);
		fillMap(ATTRIBUTES, JSGraphQLEndpointTokenTypesSets.STRING_TOKENS, STRING);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.NAMED_TYPE_DEF, DEF);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.NUMBER, NUMBER);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.TRUE, BUILTIN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.FALSE, BUILTIN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LBRACE, BRACE);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.RBRACE, BRACE);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LPAREN, PAREN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.RPAREN, PAREN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LBRACKET, BRACKET);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.RBRACKET, BRACKET);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LINE_COMMENT, COMMENT);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.AT_ANNOTATION, DefaultLanguageHighlighterColors.METADATA);
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer() {
		return new FlexAdapter(new JSGraphQLEndpointLexer());
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		return pack(ATTRIBUTES.get(tokenType));
	}
}
