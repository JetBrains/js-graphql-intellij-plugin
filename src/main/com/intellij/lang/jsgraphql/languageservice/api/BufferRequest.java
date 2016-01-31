/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.languageservice.api;

public class BufferRequest extends Request {

    private String buffer;
    private int line;
    private int ch;

    protected BufferRequest(String command, boolean relay) {
        super(command, relay);
    }

    public static BufferRequest getTokens(String buffer) {
        BufferRequest ret = new BufferRequest("getTokens", true);
        ret.buffer = buffer;
        return ret;
    }

    public static BufferRequest getHints(String buffer, int line, int ch, boolean relay) {
        BufferRequest ret = new BufferRequest("getHints", relay);
        ret.buffer = buffer;
        ret.line = line;
        ret.ch = ch;
        return ret;
    }

    public static BufferRequest getTokenDocumentation(String buffer, int line, int ch, boolean relay) {
        BufferRequest ret = new BufferRequest("getTokenDocumentation", relay);
        ret.buffer = buffer;
        ret.line = line;
        ret.ch = ch;
        return ret;
    }

    public static BufferRequest getAnnotations(String buffer, boolean relay) {
        BufferRequest ret = new BufferRequest("getAnnotations", relay);
        ret.buffer = buffer;
        return ret;
    }

    public static BufferRequest getAST(String buffer, boolean relay) {
        BufferRequest ret = new BufferRequest("getAST", relay);
        ret.buffer = buffer;
        return ret;
    }

    public static BufferRequest getSchemaTokensAndAST(String buffer, boolean relay) {
        BufferRequest ret = new BufferRequest("getSchemaTokensAndAST", relay);
        ret.buffer = buffer;
        return ret;
    }

}
