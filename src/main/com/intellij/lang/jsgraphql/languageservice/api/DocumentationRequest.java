/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import org.jetbrains.annotations.NotNull;

public class DocumentationRequest extends Request {

    private String type;
    private String field;

    protected DocumentationRequest(String command) {
        super(command, null);
    }

    public static DocumentationRequest getTypeDocumentation(@NotNull String type) {
        DocumentationRequest req = new DocumentationRequest("getTypeDocumentation");
        req.type = type;
        return req;
    }

    public static DocumentationRequest getFieldDocumentation(@NotNull String type, @NotNull String field) {
        DocumentationRequest req = new DocumentationRequest("getFieldDocumentation");
        req.type = type;
        req.field = field;
        return req;
    }
}
