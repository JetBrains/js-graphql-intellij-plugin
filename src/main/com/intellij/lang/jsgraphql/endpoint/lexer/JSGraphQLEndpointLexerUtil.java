/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.lexer;

public class JSGraphQLEndpointLexerUtil {

	/**
	 * Returns whether a matched keyword is considered a keyword at the marked position.
	 * If the token is used as a field name or argument name, it's considered an identifier token.
	 * @param zzBuffer the buffer that contains the tokens
	 * @param zzMarkedPos the position of the next character after the matched token
	 * @return true if the matched token is considered a keyword at the specified position
	 */
	public static boolean isKeywordAtPos(CharSequence zzBuffer, int zzMarkedPos) {
		final int length = zzBuffer.length();
		int pos = zzMarkedPos;
		while (pos < length) {
			char nextChar = zzBuffer.charAt(pos);
			switch (nextChar) {
				case ' ':
				case '\n':
				case '\r':
				case '\t':
					pos++;
					break;
				case ':':
					// not a keyword if a colon follows, e.g. 'type Foo { type: String }' or 'type Foo { field(type: String): String }'
					//                                                        ^                                    ^
					return false;
				case '(':
					// not a keyword if an opening parenthesis follows, e.g. 'type Foo { type(arg: Int): String }'
					//                                                                       ^
					return false;
				default:
					return true;
			}
		}
		return true;
	}

}
