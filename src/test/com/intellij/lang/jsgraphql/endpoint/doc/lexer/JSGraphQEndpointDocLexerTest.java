/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public class JSGraphQEndpointDocLexerTest extends LexerTestCase {

	@Override
	protected Lexer createLexer() {
		return new FlexAdapter(new JSGraphQLEndpointDocLexer());
	}

	@Override
	protected String getDirPath() {
		return "";
	}

	public void testLexer() {

		doTest("@param foo This is the desc", "docName ('@param')\n" +
				"WHITE_SPACE (' ')\n" +
				"docValue ('foo')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('This')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('is')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('the')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('desc')");

		doTest("This is the desc", "docText ('This')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('is')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('the')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('desc')");


		doTest("@param foo This is the desc with another @param", "docName ('@param')\n" +
				"WHITE_SPACE (' ')\n" +
				"docValue ('foo')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('This')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('is')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('the')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('desc')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('with')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('another')\n" +
				"WHITE_SPACE (' ')\n" +
				"docText ('@param')");

	}

}
