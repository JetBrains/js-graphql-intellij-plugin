/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.schema;

import com.intellij.lang.Language;

public class JSGraphQLSchemaLanguage extends Language {
    public static final JSGraphQLSchemaLanguage INSTANCE = new JSGraphQLSchemaLanguage();
    public static final String LANGUAGE_ID = "GraphQL Schema";

    private JSGraphQLSchemaLanguage() {
        super(LANGUAGE_ID);
    }
}