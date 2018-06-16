/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.languageservice.api;

public class BufferRequest extends Request {

    private String buffer;
    private int line;
    private int ch;

    protected BufferRequest(String command, String environment) {
        super(command, environment);
    }

    public static BufferRequest getTokens(String buffer, String environment) {
        BufferRequest ret = new BufferRequest("getTokens", environment);
        ret.buffer = buffer;
        return ret;
    }

    public static BufferRequest getHints(String buffer, int line, int ch, String environment) {
        BufferRequest ret = new BufferRequest("getHints", environment);
        ret.buffer = buffer;
        ret.line = line;
        ret.ch = ch;
        return ret;
    }

    public static BufferRequest getTokenDocumentation(String buffer, int line, int ch, String environment) {
        BufferRequest ret = new BufferRequest("getTokenDocumentation", environment);
        ret.buffer = buffer;
        ret.line = line;
        ret.ch = ch;
        return ret;
    }

    public static BufferRequest getAnnotations(String buffer, String environment) {
        BufferRequest ret = new BufferRequest("getAnnotations", environment);
        ret.buffer = buffer;
        return ret;
    }

}
