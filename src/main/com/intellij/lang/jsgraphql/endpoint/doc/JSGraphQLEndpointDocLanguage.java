/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint.doc;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;

public class JSGraphQLEndpointDocLanguage extends Language implements InjectableLanguage {
	public static final JSGraphQLEndpointDocLanguage INSTANCE = new JSGraphQLEndpointDocLanguage();
	public static final String LANGUAGE_ID = "GraphQL Endpoint Doc";

	private JSGraphQLEndpointDocLanguage() {
		super(LANGUAGE_ID);
	}
}
