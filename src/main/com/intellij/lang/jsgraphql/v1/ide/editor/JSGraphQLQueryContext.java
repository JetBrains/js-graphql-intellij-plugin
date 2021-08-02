/*
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.ide.editor;

public class JSGraphQLQueryContext {

    public String query;
    public Runnable onError;

    public JSGraphQLQueryContext(String query, Runnable onError) {
        this.query = query;
        this.onError = onError;
    }
}
