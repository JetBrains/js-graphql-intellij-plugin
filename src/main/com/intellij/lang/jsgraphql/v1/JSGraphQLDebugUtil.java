/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1;

public class JSGraphQLDebugUtil {

    public static final String GRAPHQL_DEBUG = "jsgraphql.debug";
    public static final String GRAPHQL_DEBUG_LANGUAGE_SERVICE_URL = "jsgraphql.debug.languageServiceUrl";
    public static final String GRAPHQL_DEBUG_LANGUAGE_SERVICE_DIST_FILE = "jsgraphql.debug.languageServiceDistFile";

    public final static boolean debug = "true".equals(System.getProperty(GRAPHQL_DEBUG));

    public final static String languageServiceUrl = (String)System.getProperty(GRAPHQL_DEBUG_LANGUAGE_SERVICE_URL);

    public final static String languageServiceDistFile = (String)System.getProperty(GRAPHQL_DEBUG_LANGUAGE_SERVICE_DIST_FILE);
}
