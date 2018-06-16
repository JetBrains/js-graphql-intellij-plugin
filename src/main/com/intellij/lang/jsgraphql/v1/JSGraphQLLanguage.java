/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1;

import com.intellij.lang.Language;

public class JSGraphQLLanguage extends Language {
    public static final JSGraphQLLanguage INSTANCE = new JSGraphQLLanguage();
    public static final String LANGUAGE_ID = "GraphQL v1";

    private JSGraphQLLanguage() {
        super(LANGUAGE_ID);
    }
}