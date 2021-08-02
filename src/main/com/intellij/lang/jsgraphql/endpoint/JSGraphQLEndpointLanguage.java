/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.endpoint;

import com.intellij.lang.Language;

public class JSGraphQLEndpointLanguage extends Language {
    public static final JSGraphQLEndpointLanguage INSTANCE = new JSGraphQLEndpointLanguage();
    public static final String LANGUAGE_ID = "GraphQL Endpoint";

    private JSGraphQLEndpointLanguage() {
        super(LANGUAGE_ID);
    }
}
