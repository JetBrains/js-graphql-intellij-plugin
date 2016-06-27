/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 * <p>
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.ide.highlighting;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes;
import com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypesSets;
import com.intellij.lang.jsgraphql.endpoint.lexer.JSGraphQLEndpointLexer;
import com.intellij.lang.jsgraphql.ide.highlighting.JSGraphQLSyntaxHighlighter;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointSyntaxHighlighter extends SyntaxHighlighterBase {

	private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<>();

	static {
		fillMap(ATTRIBUTES, JSGraphQLEndpointTokenTypesSets.KEYWORDS, JSGraphQLSyntaxHighlighter.KEYWORD);
		fillMap(ATTRIBUTES, JSGraphQLEndpointTokenTypesSets.PUNCTUATION, JSGraphQLSyntaxHighlighter.PUNCTUATION);
		fillMap(ATTRIBUTES, JSGraphQLEndpointTokenTypesSets.STRING_TOKENS, JSGraphQLSyntaxHighlighter.STRING);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.NAMED_TYPE_DEF, JSGraphQLSyntaxHighlighter.DEF);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.NUMBER, JSGraphQLSyntaxHighlighter.NUMBER);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.TRUE, JSGraphQLSyntaxHighlighter.BUILTIN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.FALSE, JSGraphQLSyntaxHighlighter.BUILTIN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LBRACE, JSGraphQLSyntaxHighlighter.BRACE);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.RBRACE, JSGraphQLSyntaxHighlighter.BRACE);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LPAREN, JSGraphQLSyntaxHighlighter.PAREN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.RPAREN, JSGraphQLSyntaxHighlighter.PAREN);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LBRACKET, JSGraphQLSyntaxHighlighter.BRACKET);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.RBRACKET, JSGraphQLSyntaxHighlighter.BRACKET);
		ATTRIBUTES.put(JSGraphQLEndpointTokenTypes.LINE_COMMENT, JSGraphQLSyntaxHighlighter.COMMENT);
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