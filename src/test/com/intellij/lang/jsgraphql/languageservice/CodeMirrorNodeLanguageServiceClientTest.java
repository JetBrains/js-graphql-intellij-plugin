/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice;

import com.intellij.lang.jsgraphql.v1.JSGraphQLDebugUtil;

import java.net.URL;

public class CodeMirrorNodeLanguageServiceClientTest {

    public static void setLanguageServiceUrl(URL languageServiceUrl) {
        if(languageServiceUrl == null) {
            System.clearProperty(JSGraphQLDebugUtil.GRAPHQL_DEBUG);
            System.clearProperty(JSGraphQLDebugUtil.GRAPHQL_DEBUG_LANGUAGE_SERVICE_URL);
        } else {
            System.setProperty(JSGraphQLDebugUtil.GRAPHQL_DEBUG, "true");
            System.setProperty(JSGraphQLDebugUtil.GRAPHQL_DEBUG_LANGUAGE_SERVICE_URL, languageServiceUrl.toString());
        }

    }

}
