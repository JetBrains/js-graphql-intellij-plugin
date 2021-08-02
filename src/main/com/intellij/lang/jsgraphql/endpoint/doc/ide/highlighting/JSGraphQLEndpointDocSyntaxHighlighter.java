/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.ide.highlighting;

import com.intellij.lang.jsgraphql.endpoint.ide.highlighting.JSGraphQLEndpointSyntaxHighlighter;
import org.jetbrains.annotations.NotNull;

import com.intellij.lang.jsgraphql.endpoint.doc.lexer.JSGraphQLEndpointDocLexer;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

public class JSGraphQLEndpointDocSyntaxHighlighter extends SyntaxHighlighterBase {

	@NotNull
	@Override
	public Lexer getHighlightingLexer() {
		return new FlexAdapter(new JSGraphQLEndpointDocLexer());
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
		// highlight as comment until highlight annotator does a pass
		return JSGraphQLEndpointSyntaxHighlighter.COMMENT_KEYS;
	}
}
