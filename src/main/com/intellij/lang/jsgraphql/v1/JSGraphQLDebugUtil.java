/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1;

public class JSGraphQLDebugUtil {

    public static final String GRAPHQL_DEBUG = "jsgraphql.debug";

    public final static boolean debug = "true".equals(System.getProperty(GRAPHQL_DEBUG));

}
