/**
 *  Copyright (c) 2015, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

import org.jetbrains.annotations.NotNull;

public class DocumentationRequest extends Request {

    private String type;

    protected DocumentationRequest(String command) {
        super(command, true);
    }

    public static DocumentationRequest getTypeDocumentation(@NotNull String type) {
        DocumentationRequest req = new DocumentationRequest("getTypeDocumentation");
        req.type = type;
        return req;
    }
}
