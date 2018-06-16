/**
 *  Copyright (c) 2015-present, Jim Kynde Meyer
 *  All rights reserved.
 *
 *  This source code is licensed under the MIT license found in the
 *  LICENSE file in the root directory of this source tree.
 */
package com.intellij.lang.jsgraphql.v1.languageservice.api;

public class Token {

    String type;
    String text;
    int start;
    int end;
    String kind;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }
    public void setEnd(int end) {
        this.end = end;
    }

    public String getKind() {
        return kind;
    }
    public void setKind(String kind) {
        this.kind = kind;
    }

    public Token withTextAndOffset(String text, int offset) {
        final Token token = new Token();
        token.type = type;
        token.text = text;
        token.kind = kind;
        token.start = start + offset;
        token.end = token.start + text.length();
        return token;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type='" + type + '\'' +
                ", text='" + text + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", kind='" + kind + '\'' +
                '}';
    }
}
