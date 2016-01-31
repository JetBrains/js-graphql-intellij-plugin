/**
 * Copyright (c) 2015-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

public class SchemaWithVersionRequest extends Request {

    public final static SchemaWithVersionRequest INSTANCE = new SchemaWithVersionRequest();

    private SchemaWithVersionRequest() {
        super("getSchemaWithVersion", false);
    }
}
