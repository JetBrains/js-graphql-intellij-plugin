/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.lexer;

import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Resources;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.testFramework.LexerTestCase;

public class JSGraphQEndpointLexerTest extends LexerTestCase {

	@Override
	protected Lexer createLexer() {
		return new FlexAdapter(new JSGraphQLEndpointLexer());
	}

	@Override
	protected String getDirPath() {
		return "";
	}

	public void testLexer() throws IOException {
		final String spec = getTestResource("ParsingTestData.graphqle");
		final String expected = getTestResource("ParsingTestData.lexer.txt");
		doTest(spec, expected);
	}

	public void testStringTermination() {
		doTest("import \"foo\ntype Bar", "import ('import')\n" +
				"WHITE_SPACE (' ')\n" +
				"OPEN_QUOTE ('\"')\n" +
				"STRING_BODY ('foo')\n" +
				"BAD_CHARACTER ('\\n')\n" +
				"type ('type')\n" +
				"WHITE_SPACE (' ')\n" +
				"identifier ('Bar')");
	}

	private String getTestResource(String name) throws IOException {
		return Resources.toString(Resources.getResource(this.getClass(), "/testData/endpoint/" + name), Charset.forName("ISO-8859-1")).replace("\r\n", "\n");
	}
}
