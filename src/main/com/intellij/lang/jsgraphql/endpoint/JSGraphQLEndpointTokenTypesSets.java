/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import static com.intellij.lang.jsgraphql.endpoint.JSGraphQLEndpointTokenTypes.*;

import com.intellij.psi.tree.TokenSet;

public interface JSGraphQLEndpointTokenTypesSets {

	TokenSet KEYWORDS = TokenSet.create(
			TYPE,
			INTERFACE,
			INPUT,
			ENUM,
			UNION,
			SCALAR,
			IMPLEMENTS,
			IMPORT,
			SCHEMA,
			QUERY,
			MUTATION,
			SUBSCRIPTION,
			TRUE,
			FALSE,
			ANNOTATION_DEF
	);

	TokenSet PUNCTUATION = TokenSet.create(
			COMMA,
			COLON,
			EQUALS,
			REQUIRED
	);

	TokenSet STRING_TOKENS = TokenSet.create(
			OPEN_QUOTE,
			STRING_BODY,
			CLOSING_QUOTE
	);
}
