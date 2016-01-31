/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

public class Annotation {

    private String message;
    private String severity;
    private String type;
    private Pos from;
    private Pos to;

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public String getType() {
        return type;
    }

    public Pos getFrom() {
        return from;
    }

    public Pos getTo() {
        return to;
    }
}
